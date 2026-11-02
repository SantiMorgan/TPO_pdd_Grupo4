package ar.edu.uade.barstock.model.persistence;

import ar.edu.uade.barstock.model.inventario.TipoMovimiento;
import ar.edu.uade.barstock.model.reporte.DatosReporte;
import ar.edu.uade.barstock.model.reporte.FuenteDatosReporte;
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
    public DatosReporte obtener(UUID establecimientoId) {
        return new DatosReporte(
                comandas.findByEstablecimientoIdAndDespachadaEnIsNotNullOrderByDespachadaEnAsc(establecimientoId),
                movimientos.findByEstablecimientoIdAndTipo(establecimientoId, TipoMovimiento.COMPRA),
                auditorias.findByEstablecimientoId(establecimientoId));
    }
}
