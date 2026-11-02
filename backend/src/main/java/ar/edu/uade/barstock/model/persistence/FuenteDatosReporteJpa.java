package ar.edu.uade.barstock.model.persistence;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import ar.edu.uade.barstock.model.inventario.TipoMovimiento;
import ar.edu.uade.barstock.model.reporte.FuenteDatosReporte;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador de lectura de los reportes sobre los repositorios JPA. */
@Component
public class FuenteDatosReporteJpa implements FuenteDatosReporte {
    private final ComandaRepository comandas;
    private final MovimientoStockRepository movimientos;
    private final AuditoriaRepository auditorias;

    public FuenteDatosReporteJpa(ComandaRepository comandas, MovimientoStockRepository movimientos,
                                 AuditoriaRepository auditorias) {
        this.comandas = comandas;
        this.movimientos = movimientos;
        this.auditorias = auditorias;
    }

    @Override
    public List<Comanda> comandasDespachadas(Collection<UUID> establecimientos, Instant desde, Instant hasta) {
        return comandas.buscarDespachadas(establecimientos, desde, hasta);
    }

    @Override
    public List<MovimientoStock> compras(Collection<UUID> establecimientos, Instant desde, Instant hasta) {
        return movimientos.buscarEntre(establecimientos, TipoMovimiento.COMPRA, desde, hasta);
    }

    @Override
    public List<Auditoria> auditorias(Collection<UUID> establecimientos, Instant desde, Instant hasta) {
        return auditorias.buscarEntre(establecimientos, desde, hasta);
    }
}
