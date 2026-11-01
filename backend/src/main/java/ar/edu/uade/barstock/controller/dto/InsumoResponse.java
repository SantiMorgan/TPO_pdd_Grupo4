package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import java.math.BigDecimal;
import java.util.UUID;

public record InsumoResponse(UUID id, String codigo, String nombre, UnidadMedida unidadMedida,
                             BigDecimal stockActual, BigDecimal costoUnitario, BigDecimal puntoReorden,
                             boolean activo, boolean necesitaReposicion) {
    public static InsumoResponse desde(Insumo insumo) {
        return new InsumoResponse(insumo.getId(), insumo.getCodigo(), insumo.getNombre(),
                insumo.getUnidadMedida(), insumo.getStockActual(), insumo.getCostoUnitario(),
                insumo.getPuntoReorden(), insumo.isActivo(), insumo.necesitaReposicion());
    }
}
