package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** {@code contenidoEnvase} es opcional: si falta, cada envase comprado suma una unidad. */
public record CrearInsumoRequest(
        @NotBlank @Size(max = 50) String codigo,
        @NotBlank @Size(max = 150) String nombre,
        @NotNull UnidadMedida unidadMedida,
        @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 0) BigDecimal stockInicial,
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal costoUnitario,
        @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 0) BigDecimal puntoReorden,
        @Min(1) @Max(999999) Integer contenidoEnvase) {
    public int contenidoEnvaseOPorDefecto() {
        return contenidoEnvase == null ? 1 : contenidoEnvase;
    }
}
