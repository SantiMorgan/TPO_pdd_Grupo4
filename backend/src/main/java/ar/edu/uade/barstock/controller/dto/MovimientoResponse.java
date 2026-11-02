package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import ar.edu.uade.barstock.model.inventario.TipoMovimiento;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovimientoResponse(UUID id, UUID insumoId, String insumo, UnidadMedida unidadMedida,
                                 TipoMovimiento tipo, BigDecimal cantidad, BigDecimal stockResultante,
                                 BigDecimal monto, String detalle, Instant fecha) {
    public static MovimientoResponse desde(MovimientoStock movimiento) {
        return new MovimientoResponse(movimiento.getId(), movimiento.getInsumoId(), movimiento.getNombreInsumo(),
                movimiento.getUnidadMedida(), movimiento.getTipo(), movimiento.getCantidad(),
                movimiento.getStockResultante(), movimiento.getMonto(), movimiento.getDetalle(), movimiento.getFecha());
    }
}
