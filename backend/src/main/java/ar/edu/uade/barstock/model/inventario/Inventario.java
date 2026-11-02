package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.persistence.InsumoRepository;
import ar.edu.uade.barstock.model.persistence.MovimientoStockRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Sujeto del patrón Observer y responsable de todo cambio de stock.
 * Los descuentos son atómicos: bloquea los insumos en orden de id (evita deadlocks), valida todos
 * y recién entonces descuenta. Los observadores se avisan después del commit (ADR-001 §6).
 */
@Service
@Transactional(readOnly = true)
public class Inventario {
    private static final Logger log = LoggerFactory.getLogger(Inventario.class);
    private static final int MAXIMO_ENVASES_POR_COMPRA = 500;

    private final InsumoRepository insumos;
    private final MovimientoStockRepository movimientos;
    private final Clock reloj;
    private final List<ObservadorStock> observadores = new CopyOnWriteArrayList<>();

    public Inventario(InsumoRepository insumos, MovimientoStockRepository movimientos, Clock reloj,
                      List<ObservadorStock> observadores) {
        this.insumos = insumos;
        this.movimientos = movimientos;
        this.reloj = reloj;
        this.observadores.addAll(observadores);
    }

    public void agregarObservador(ObservadorStock observador) {
        observadores.add(observador);
    }

    public void quitarObservador(ObservadorStock observador) {
        observadores.remove(observador);
    }

    /** Comprueba sin bloquear ni reservar: el stock se descuenta recién al despachar. */
    public void verificarDisponibilidad(UUID establecimientoId, List<Consumo> consumos) {
        List<Faltante> faltantes = new ArrayList<>();
        for (Consumo consumo : agrupar(consumos).values()) {
            Insumo insumo = insumos.findById(consumo.insumoId())
                    .filter(i -> i.perteneceA(establecimientoId))
                    .orElseThrow(() -> new InsumoNoEncontradoException(consumo.insumoId()));
            validarUso(insumo, consumo);
            if (!insumo.alcanza(consumo.cantidad())) {
                faltantes.add(Faltante.de(insumo, consumo.cantidad()));
            }
        }
        if (!faltantes.isEmpty()) {
            throw new StockInsuficienteException(faltantes);
        }
    }

    @Transactional
    public void descontarTodo(UUID establecimientoId, List<Consumo> consumos, String detalle) {
        Map<UUID, Consumo> totales = agrupar(consumos);
        List<Insumo> bloqueados = bloquearEnOrden(establecimientoId, totales.keySet());
        List<Faltante> faltantes = new ArrayList<>();
        for (Insumo insumo : bloqueados) {
            Consumo consumo = totales.get(insumo.getId());
            validarUso(insumo, consumo);
            if (!insumo.alcanza(consumo.cantidad())) {
                faltantes.add(Faltante.de(insumo, consumo.cantidad()));
            }
        }
        if (!faltantes.isEmpty()) {
            log.warn("Despacho rechazado por stock insuficiente: {}", faltantes);
            throw new StockInsuficienteException(faltantes);
        }
        Instant ahora = Instant.now(reloj);
        List<EventoStock> eventos = new ArrayList<>();
        for (Insumo insumo : bloqueados) {
            BigDecimal anterior = insumo.getStockActual();
            BigDecimal cantidad = totales.get(insumo.getId()).cantidad();
            insumo.descontar(cantidad);
            movimientos.save(MovimientoStock.consumo(insumo, cantidad, detalle, ahora));
            eventos.add(EventoStock.de(insumo, anterior));
        }
        notificar(eventos);
    }

    @Transactional
    public MovimientoStock registrarCompra(UUID establecimientoId, UUID insumoId, int envases) {
        if (envases < 1 || envases > MAXIMO_ENVASES_POR_COMPRA) {
            throw new IllegalArgumentException("La compra debe ser de 1 a " + MAXIMO_ENVASES_POR_COMPRA + " envases");
        }
        Insumo insumo = bloquear(establecimientoId, insumoId);
        if (!insumo.isActivo()) {
            throw new InsumoInactivoException(insumo.getNombre());
        }
        BigDecimal anterior = insumo.getStockActual();
        BigDecimal cantidad = BigDecimal.valueOf((long) envases * insumo.getContenidoEnvase());
        insumo.reponer(cantidad);
        MovimientoStock movimiento = movimientos.save(MovimientoStock.compra(insumo, cantidad,
                envases + " envase(s) de " + insumo.getContenidoEnvase() + " " + insumo.getUnidadMedida().simbolo(),
                Instant.now(reloj)));
        notificar(List.of(EventoStock.de(insumo, anterior)));
        return movimiento;
    }

    @Transactional
    public List<AjusteStock> ajustarARecuento(UUID establecimientoId, Map<UUID, BigDecimal> recuento, String detalle) {
        if (recuento == null || recuento.isEmpty()) {
            throw new IllegalArgumentException("Ingresá por lo menos un recuento físico");
        }
        Instant ahora = Instant.now(reloj);
        List<AjusteStock> ajustes = new ArrayList<>();
        List<EventoStock> eventos = new ArrayList<>();
        for (Insumo insumo : bloquearEnOrden(establecimientoId, recuento.keySet())) {
            BigDecimal teorico = insumo.getStockActual();
            BigDecimal diferencia = insumo.ajustarA(recuento.get(insumo.getId()));
            if (diferencia.signum() != 0) {
                movimientos.save(MovimientoStock.ajuste(insumo, diferencia, detalle, ahora));
                eventos.add(EventoStock.de(insumo, teorico));
            }
            ajustes.add(new AjusteStock(insumo.getId(), insumo.getNombre(), insumo.getUnidadMedida(),
                    teorico, insumo.getStockActual(), insumo.getCostoUnitario()));
        }
        notificar(eventos);
        return ajustes;
    }

    public List<MovimientoStock> ultimosMovimientos(UUID establecimientoId, int limite) {
        return movimientos.findByEstablecimientoIdOrderByFechaDesc(establecimientoId,
                PageRequest.of(0, Math.max(1, Math.min(limite, 200))));
    }

    private void validarUso(Insumo insumo, Consumo consumo) {
        if (!insumo.isActivo()) {
            throw new InsumoInactivoException(insumo.getNombre());
        }
        if (insumo.getUnidadMedida() != consumo.unidad()) {
            throw new UnidadIncompatibleException(insumo.getId(), insumo.getUnidadMedida(), consumo.unidad());
        }
    }

    /** Suma los consumos del mismo insumo: dos tragos con gin se validan contra el stock total. */
    private static Map<UUID, Consumo> agrupar(List<Consumo> consumos) {
        if (consumos == null || consumos.isEmpty()) {
            throw new IllegalArgumentException("No hay insumos para descontar");
        }
        Map<UUID, Consumo> totales = new TreeMap<>();
        for (Consumo consumo : consumos) {
            totales.merge(consumo.insumoId(), consumo, Consumo::sumar);
        }
        return totales;
    }

    private List<Insumo> bloquearEnOrden(UUID establecimientoId, Collection<UUID> ids) {
        return ids.stream().sorted().map(id -> bloquear(establecimientoId, id)).toList();
    }

    private Insumo bloquear(UUID establecimientoId, UUID insumoId) {
        return insumos.buscarParaActualizar(insumoId)
                .filter(insumo -> insumo.perteneceA(establecimientoId))
                .orElseThrow(() -> new InsumoNoEncontradoException(insumoId));
    }

    private void notificar(List<EventoStock> eventos) {
        if (eventos.isEmpty()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    avisar(eventos);
                }
            });
        } else {
            avisar(eventos);
        }
    }

    /** Un observador que falla no deshace el cambio ya confirmado ni impide avisar a los demás. */
    private void avisar(List<EventoStock> eventos) {
        for (EventoStock evento : eventos) {
            for (ObservadorStock observador : observadores) {
                try {
                    observador.actualizar(evento);
                } catch (RuntimeException ex) {
                    log.warn("El observador {} falló con el insumo {}: {}",
                            observador.getClass().getSimpleName(), evento.nombreInsumo(), ex.getMessage());
                }
            }
        }
    }
}
