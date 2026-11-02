package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.comanda.OrigenComanda;
import ar.edu.uade.barstock.model.receta.CantidadReceta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** {@code cliente} es el nombre o la mesa con la que se identifica el pedido. */
public record RegistrarComandaRequest(
        @NotNull OrigenComanda origen,
        @Size(max = 60) String cliente,
        @NotEmpty @Size(max = 20) List<@Valid @NotNull Item> items) {

    public record Item(@NotNull UUID recetaId, @NotNull @Min(1) @Max(99) Integer cantidad) {
    }

    public List<CantidadReceta> pedido() {
        return items.stream().map(item -> new CantidadReceta(item.recetaId(), item.cantidad())).toList();
    }
}
