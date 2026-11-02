package ar.edu.uade.barstock.model.reporte;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Rango de jornadas con los dos extremos incluidos. */
public record Periodo(LocalDate desde, LocalDate hasta) {
    public static final int MAXIMO_DIAS = 366;

    public Periodo {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Indicá desde y hasta qué jornada reportar");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) + 1 > MAXIMO_DIAS) {
            throw new IllegalArgumentException("El período puede abarcar hasta " + MAXIMO_DIAS + " jornadas");
        }
    }

    public static Periodo de(LocalDate jornada) {
        return new Periodo(jornada, jornada);
    }

    public int dias() {
        return (int) ChronoUnit.DAYS.between(desde, hasta) + 1;
    }

    /** Las mismas cantidad de jornadas, justo antes de este período. */
    public Periodo anterior() {
        return new Periodo(desde.minusDays(dias()), desde.minusDays(1));
    }

    public boolean contiene(LocalDate jornada) {
        return !jornada.isBefore(desde) && !jornada.isAfter(hasta);
    }

    public List<LocalDate> jornadas() {
        return desde.datesUntil(hasta.plusDays(1)).toList();
    }
}
