package ar.edu.uade.barstock.model.establecimiento;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Apertura y cierre de los turnos de mediodía y noche.
 * La jornada laboral va de 6:00 a 5:59 del día siguiente: lo que pasa después de medianoche
 * pertenece a la jornada que empezó el día anterior. Lo registrado fuera de horario (por ejemplo,
 * el recuento después del cierre) se asigna al turno más cercano.
 */
@Embeddable
public class HorarioAtencion {
    public static final LocalTime INICIO_JORNADA = LocalTime.of(6, 0);
    private static final int MINUTOS_DIA = 24 * 60;

    @Column(name = "mediodia_apertura", nullable = false)
    private LocalTime mediodiaApertura;

    @Column(name = "mediodia_cierre", nullable = false)
    private LocalTime mediodiaCierre;

    @Column(name = "noche_apertura", nullable = false)
    private LocalTime nocheApertura;

    @Column(name = "noche_cierre", nullable = false)
    private LocalTime nocheCierre;

    protected HorarioAtencion() {
        // Constructor requerido por JPA.
    }

    public HorarioAtencion(LocalTime mediodiaApertura, LocalTime mediodiaCierre,
                           LocalTime nocheApertura, LocalTime nocheCierre) {
        if (mediodiaApertura == null || mediodiaCierre == null || nocheApertura == null || nocheCierre == null) {
            throw new IllegalArgumentException("Indicá la apertura y el cierre de los dos turnos");
        }
        this.mediodiaApertura = mediodiaApertura.truncatedTo(ChronoUnit.MINUTES);
        this.mediodiaCierre = mediodiaCierre.truncatedTo(ChronoUnit.MINUTES);
        this.nocheApertura = nocheApertura.truncatedTo(ChronoUnit.MINUTES);
        this.nocheCierre = nocheCierre.truncatedTo(ChronoUnit.MINUTES);
        boolean ordenado = enJornada(this.mediodiaApertura) < enJornada(this.mediodiaCierre)
                && enJornada(this.mediodiaCierre) <= enJornada(this.nocheApertura)
                && enJornada(this.nocheApertura) < enJornada(this.nocheCierre);
        if (!ordenado) {
            throw new IllegalArgumentException("Los horarios tienen que seguir el orden de la jornada (de 6:00 a 5:59): "
                    + "primero abre y cierra el mediodía y después abre y cierra la noche");
        }
    }

    /** Horarios comunes de un bar: mediodía de 12 a 16 y noche de 20 a 2. */
    public static HorarioAtencion porDefecto() {
        return new HorarioAtencion(LocalTime.of(12, 0), LocalTime.of(16, 0), LocalTime.of(20, 0), LocalTime.of(2, 0));
    }

    /** Antes de las 6:00 todavía es la jornada del día anterior. */
    public static LocalDate jornadaDe(LocalDateTime momento) {
        LocalDate fecha = momento.toLocalDate();
        return momento.toLocalTime().isBefore(INICIO_JORNADA) ? fecha.minusDays(1) : fecha;
    }

    public static LocalDateTime inicioDe(LocalDate jornada) {
        return jornada.atTime(INICIO_JORNADA);
    }

    public Turno turnoDe(LocalTime hora) {
        int minuto = enJornada(hora);
        int aMediodia = distancia(minuto, enJornada(mediodiaApertura), enJornada(mediodiaCierre));
        int aNoche = distancia(minuto, enJornada(nocheApertura), enJornada(nocheCierre));
        return aNoche == 0 || aNoche < aMediodia ? Turno.NOCHE : Turno.MEDIODIA;
    }

    public LocalTime apertura(Turno turno) {
        return turno == Turno.MEDIODIA ? mediodiaApertura : nocheApertura;
    }

    public LocalTime cierre(Turno turno) {
        return turno == Turno.MEDIODIA ? mediodiaCierre : nocheCierre;
    }

    /** Minutos desde el inicio de la jornada (6:00 → 0, 2:00 → 1200). */
    private static int enJornada(LocalTime hora) {
        return Math.floorMod(hora.getHour() * 60 + hora.getMinute() - INICIO_JORNADA.getHour() * 60, MINUTOS_DIA);
    }

    /** 0 si el minuto cae dentro del turno; si no, cuánto falta o cuánto pasó desde su borde más cercano. */
    private static int distancia(int minuto, int desde, int hasta) {
        if (minuto < desde) {
            return desde - minuto;
        }
        return minuto > hasta ? minuto - hasta : 0;
    }

    public LocalTime getMediodiaApertura() { return mediodiaApertura; }
    public LocalTime getMediodiaCierre() { return mediodiaCierre; }
    public LocalTime getNocheApertura() { return nocheApertura; }
    public LocalTime getNocheCierre() { return nocheCierre; }
}
