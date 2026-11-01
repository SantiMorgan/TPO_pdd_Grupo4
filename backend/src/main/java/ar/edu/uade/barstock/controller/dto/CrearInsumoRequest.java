package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CrearInsumoRequest(
        @NotBlank @Size(max = 50) String codigo,
        @NotBlank @Size(max = 150) String nombre,
        @NotNull UnidadMedida unidadMedida,
        @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 0) BigDecimal stockInicial,
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal costoUnitario,
        @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 0) BigDecimal puntoReorden) {
}
