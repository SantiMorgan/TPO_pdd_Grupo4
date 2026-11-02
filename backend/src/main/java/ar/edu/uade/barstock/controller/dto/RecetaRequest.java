package ar.edu.uade.barstock.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RecetaRequest(
        @NotBlank @Size(max = 80) String nombre,
        @Size(max = 200) String descripcion,
        @Size(max = 16) String icono,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal precioBase,
        @NotEmpty @Size(max = 15) List<@Valid @NotNull Ingrediente> ingredientes) {

    public record Ingrediente(@NotNull UUID insumoId, @NotNull @Min(1) @Max(99999) Integer cantidad) {
    }

    /** Un insumo repetido es un error del formulario, no una suma. */
    public Map<UUID, Integer> cantidadesPorInsumo() {
        Map<UUID, Integer> cantidades = new LinkedHashMap<>();
        for (Ingrediente ingrediente : ingredientes) {
            if (cantidades.put(ingrediente.insumoId(), ingrediente.cantidad()) != null) {
                throw new IllegalArgumentException("No repitas ingredientes dentro de la misma receta");
            }
        }
        return cantidades;
    }
}
