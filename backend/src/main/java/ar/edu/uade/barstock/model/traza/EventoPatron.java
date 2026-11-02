package ar.edu.uade.barstock.model.traza;

import java.time.Instant;

public record EventoPatron(Instant fecha, String patron, String descripcion) {
}
