package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.establecimiento.HorarioAtencion;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/** Apertura y cierre de cada turno. La noche puede cerrar después de medianoche (hasta las 5:59). */
public record HorarioDto(@NotNull LocalTime mediodiaApertura, @NotNull LocalTime mediodiaCierre,
                         @NotNull LocalTime nocheApertura, @NotNull LocalTime nocheCierre) {
    public static HorarioDto desde(HorarioAtencion horario) {
        return new HorarioDto(horario.getMediodiaApertura(), horario.getMediodiaCierre(),
                horario.getNocheApertura(), horario.getNocheCierre());
    }

    public HorarioAtencion aModelo() {
        return new HorarioAtencion(mediodiaApertura, mediodiaCierre, nocheApertura, nocheCierre);
    }
}
