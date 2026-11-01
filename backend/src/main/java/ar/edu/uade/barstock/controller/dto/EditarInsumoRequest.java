package ar.edu.uade.barstock.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EditarInsumoRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal costoUnitario,
        @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 0) BigDecimal puntoReorden) {
}
