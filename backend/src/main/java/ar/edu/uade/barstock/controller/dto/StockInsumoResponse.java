package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import java.math.BigDecimal;
import java.util.UUID;

public record StockInsumoResponse(UUID id, UnidadMedida unidadMedida, BigDecimal stockActual,
                                  boolean activo) {
    public static StockInsumoResponse desde(Insumo insumo) {
        return new StockInsumoResponse(insumo.getId(), insumo.getUnidadMedida(),
                insumo.getStockActual(), insumo.isActivo());
    }
}
