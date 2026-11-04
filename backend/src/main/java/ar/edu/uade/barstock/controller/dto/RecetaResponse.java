package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.receta.Receta;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RecetaResponse(UUID id, String nombre, String descripcion, String icono, BigDecimal precioBase,
                             BigDecimal costo, boolean activa, boolean disponible,
                             List<IngredienteResponse> ingredientes) {
    public static RecetaResponse desde(Receta receta) {
        return new RecetaResponse(receta.getId(), receta.getNombre(), receta.getDescripcion(), receta.getIcono(),
                receta.getPrecioBase(), receta.calcularCosto(), receta.isActiva(), receta.disponible(),
                receta.getIngredientes().stream().map(IngredienteResponse::desde).toList());
    }
}
