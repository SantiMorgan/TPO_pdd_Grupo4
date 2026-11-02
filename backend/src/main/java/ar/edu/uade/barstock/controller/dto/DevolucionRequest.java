package ar.edu.uade.barstock.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Qué trago de la comanda se devuelve, cuántas unidades y por qué. */
public record DevolucionRequest(
        @NotNull UUID itemId,
        @NotNull @Min(1) @Max(99) Integer cantidad,
        @NotBlank @Size(min = 5, max = 220) String motivo) {
}
