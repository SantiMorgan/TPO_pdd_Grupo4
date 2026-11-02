package ar.edu.uade.barstock.model.precio;

import java.time.LocalTime;

/** La estrategia elegida para este momento y por qué se eligió. */
public record EstrategiaVigente(EstrategiaPrecio estrategia, String motivo, LocalTime hora, boolean horaSimulada) {
}
