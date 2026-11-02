package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.precio.Carta;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartaResponse(EstrategiaVigenteResponse estrategia, List<Item> items) {
    public static CartaResponse desde(Carta carta) {
        return new CartaResponse(EstrategiaVigenteResponse.desde(carta.vigente()), carta.items().stream()
                .map(item -> new Item(item.receta().getId(), item.receta().getNombre(), item.receta().getDescripcion(),
                        item.receta().getIcono(), item.receta().getPrecioBase(), item.precio(), item.costo(),
                        item.disponible(),
                        item.receta().getIngredientes().stream().map(IngredienteResponse::desde).toList()))
                .toList());
    }

    public record Item(UUID recetaId, String nombre, String descripcion, String icono, BigDecimal precioBase,
                       BigDecimal precio, BigDecimal costo, boolean disponible, List<IngredienteResponse> ingredientes) {
    }
}
