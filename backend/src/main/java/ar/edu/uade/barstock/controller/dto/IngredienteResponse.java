package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import ar.edu.uade.barstock.model.receta.IngredienteReceta;
import java.math.BigDecimal;
import java.util.UUID;

public record IngredienteResponse(UUID insumoId, String insumo, UnidadMedida unidadMedida, int cantidad,
                                  BigDecimal costo) {
    public static IngredienteResponse desde(IngredienteReceta ingrediente) {
        return new IngredienteResponse(ingrediente.getInsumo().getId(), ingrediente.getInsumo().getNombre(),
                ingrediente.getInsumo().getUnidadMedida(), ingrediente.getCantidad(), ingrediente.costo());
    }
}
