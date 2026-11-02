package ar.edu.uade.barstock.model.comanda;

import ar.edu.uade.barstock.model.comanda.state.EstadoComanda;
import ar.edu.uade.barstock.model.comanda.state.EstadosComanda;
import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.persistence.ComandaRepository;
import ar.edu.uade.barstock.model.precio.EstrategiaPrecio;
import ar.edu.uade.barstock.model.receta.Receta;
import ar.edu.uade.barstock.model.traza.TrazaPatrones;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Persistencia y transiciones de comandas. Cada transición bloquea la fila para evitar dobles despachos. */
@Service
@Transactional(readOnly = true)
public class ComandaService {
    private final ComandaRepository comandas;
    private final Clock reloj;
    private final TrazaPatrones traza;

    public ComandaService(ComandaRepository comandas, Clock reloj, TrazaPatrones traza) {
        this.comandas = comandas;
        this.reloj = reloj;
        this.traza = traza;
    }

    /** El establecimiento debe llegar bloqueado para asignar el número sin repetirlo. */
    @Transactional
    public Comanda registrar(Establecimiento establecimiento, OrigenComanda origen, String cliente,
                             EstrategiaPrecio estrategia, Map<Receta, Integer> pedido) {
        Comanda comanda = Comanda.registrar(establecimiento.getId(), establecimiento.asignarNumeroComanda(),
                origen, cliente, estrategia, pedido, Instant.now(reloj));
        comandas.save(comanda);
        traza.registrar(establecimiento.getId(), "State", "Comanda #" + comanda.getNumero() + " → Registrada");
        return comanda;
    }

    @Transactional
    public Comanda preparar(UUID establecimientoId, UUID comandaId) {
        return transicionar(establecimientoId, comandaId, comanda -> comanda.preparar(Instant.now(reloj)));
    }

    @Transactional
    public Comanda despachar(UUID establecimientoId, UUID comandaId) {
        return transicionar(establecimientoId, comandaId, comanda -> comanda.despachar(Instant.now(reloj)));
    }

    @Transactional
    public Comanda anular(UUID establecimientoId, UUID comandaId, String motivo) {
        return transicionar(establecimientoId, comandaId, comanda -> comanda.anular(motivo, Instant.now(reloj)));
    }

    public Comanda consultar(UUID establecimientoId, UUID comandaId) {
        return comandas.findById(comandaId)
                .filter(comanda -> comanda.perteneceA(establecimientoId))
                .orElseThrow(() -> new ComandaNoEncontradaException(comandaId));
    }

    public List<Comanda> listarEnCurso(UUID establecimientoId) {
        return comandas.buscarPorEstados(establecimientoId, EstadosComanda.enCurso());
    }

    public List<Comanda> listarRecientes(UUID establecimientoId, int limite) {
        return comandas.findByEstablecimientoIdOrderByNumeroDesc(establecimientoId,
                PageRequest.of(0, Math.max(1, Math.min(limite, 100))));
    }

    public long contarEnCurso(UUID establecimientoId) {
        return comandas.contarPorEstados(establecimientoId, EstadosComanda.enCurso());
    }

    private Comanda transicionar(UUID establecimientoId, UUID comandaId, Consumer<Comanda> accion) {
        Comanda comanda = comandas.buscarParaActualizar(comandaId)
                .filter(c -> c.perteneceA(establecimientoId))
                .orElseThrow(() -> new ComandaNoEncontradaException(comandaId));
        EstadoComanda anterior = comanda.getEstado();
        accion.accept(comanda);
        traza.registrar(establecimientoId, "State", "Comanda #" + comanda.getNumero() + ": "
                + anterior.nombre() + " → " + comanda.getEstado().nombre());
        return comanda;
    }
}
