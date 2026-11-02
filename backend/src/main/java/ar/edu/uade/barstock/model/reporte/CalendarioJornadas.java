package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.establecimiento.HorarioAtencion;
import ar.edu.uade.barstock.model.establecimiento.Turno;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Ubica cada operación en su jornada y en su turno, según el horario de su establecimiento. */
public final class CalendarioJornadas {
    private final ZoneId zona;
    private final Map<UUID, HorarioAtencion> horarios;

    public CalendarioJornadas(ZoneId zona, Collection<Establecimiento> establecimientos) {
        this.zona = zona;
        this.horarios = establecimientos.stream()
                .collect(Collectors.toMap(Establecimiento::getId, Establecimiento::getHorario));
    }

    public LocalDate jornadaDe(Instant instante) {
        return HorarioAtencion.jornadaDe(LocalDateTime.ofInstant(instante, zona));
    }

    public Turno turnoDe(UUID establecimientoId, Instant instante) {
        return horarios.getOrDefault(establecimientoId, HorarioAtencion.porDefecto())
                .turnoDe(LocalTime.ofInstant(instante, zona));
    }

    /** {@code turno} nulo significa los dos turnos. */
    public boolean esDelTurno(UUID establecimientoId, Instant instante, Turno turno) {
        return turno == null || turnoDe(establecimientoId, instante) == turno;
    }

    public Instant inicio(LocalDate jornada) {
        return HorarioAtencion.inicioDe(jornada).atZone(zona).toInstant();
    }

    public Instant fin(Periodo periodo) {
        return inicio(periodo.hasta().plusDays(1));
    }
}
