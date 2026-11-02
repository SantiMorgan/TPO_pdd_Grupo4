package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.precio.EstrategiaVigente;
import java.time.LocalTime;

public record EstrategiaVigenteResponse(String codigo, String nombre, String clase, String motivo,
                                        LocalTime hora, boolean horaSimulada) {
    public static EstrategiaVigenteResponse desde(EstrategiaVigente vigente) {
        return new EstrategiaVigenteResponse(vigente.estrategia().codigo(), vigente.estrategia().nombre(),
                vigente.estrategia().getClass().getSimpleName(), vigente.motivo(), vigente.hora(), vigente.horaSimulada());
    }
}
