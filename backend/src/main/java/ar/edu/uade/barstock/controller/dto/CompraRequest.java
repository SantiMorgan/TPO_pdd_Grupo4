package ar.edu.uade.barstock.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CompraRequest(@NotNull UUID insumoId, @NotNull @Min(1) @Max(500) Integer envases) {
}
