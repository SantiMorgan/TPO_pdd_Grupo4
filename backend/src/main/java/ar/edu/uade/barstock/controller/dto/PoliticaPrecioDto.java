package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.establecimiento.ModoPrecio;
import ar.edu.uade.barstock.model.establecimiento.PoliticaPrecio;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalTime;

/** Porcentajes expresados de 0 a 100 (el margen mínimo admite hasta 1000). */
public record PoliticaPrecioDto(
        @NotNull ModoPrecio modo,
        @NotNull LocalTime inicioHappyHour,
        @NotNull LocalTime finHappyHour,
        @NotNull @DecimalMin("0") @DecimalMax("90") @Digits(integer = 3, fraction = 2) BigDecimal descuentoHappyHour,
        @NotNull @DecimalMin("0") @DecimalMax("1000") @Digits(integer = 4, fraction = 2) BigDecimal margenMinimo,
        LocalTime horaSimulada) {

    public static PoliticaPrecioDto desde(PoliticaPrecio politica) {
        return new PoliticaPrecioDto(politica.getModo(), politica.getInicioHappyHour(), politica.getFinHappyHour(),
                politica.getDescuentoHappyHour(), politica.getMargenMinimo(), politica.getHoraSimulada());
    }

    public PoliticaPrecio aModelo() {
        return new PoliticaPrecio(modo, inicioHappyHour, finHappyHour, descuentoHappyHour, margenMinimo, horaSimulada);
    }
}
