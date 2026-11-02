package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.traza.EventoPatron;
import java.time.Instant;

public record EventoPatronResponse(Instant fecha, String patron, String descripcion) {
    public static EventoPatronResponse desde(EventoPatron evento) {
        return new EventoPatronResponse(evento.fecha(), evento.patron(), evento.descripcion());
    }
}
