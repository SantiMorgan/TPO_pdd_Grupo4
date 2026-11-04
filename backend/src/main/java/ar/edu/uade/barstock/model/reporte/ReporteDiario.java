package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.establecimiento.Turno;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Reporte de una jornada laboral: de las 6:00 de {@code jornada} a las 5:59 del día siguiente.
 * Una venta del 2/11 a las 23 y otra del 3/11 a las 2 quedan juntas en la jornada del 2/11.
 */
public record ReporteDiario(LocalDate jornada, Instant desde, Instant hasta, Indicadores total,
                            List<ResumenTurno> turnos, List<ReporteConGrafico.Barra> ranking) {
    public record ResumenTurno(Turno turno, Indicadores indicadores) {
    }
}
