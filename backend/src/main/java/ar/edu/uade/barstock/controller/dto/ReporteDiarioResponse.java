package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.reporte.Indicadores;
import ar.edu.uade.barstock.model.reporte.ReporteConGrafico;
import ar.edu.uade.barstock.model.reporte.ReporteDiario;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Una jornada (de 6:00 a 5:59 del día siguiente) con el total y cada turno por separado. */
public record ReporteDiarioResponse(LocalDate jornada, Instant desde, Instant hasta, Indicadores total,
                                    List<Turno> turnos, List<ReporteConGrafico.Barra> ranking) {
    public static ReporteDiarioResponse desde(ReporteDiario reporte) {
        return new ReporteDiarioResponse(reporte.jornada(), reporte.desde(), reporte.hasta(), reporte.total(),
                reporte.turnos().stream().map(t -> new Turno(t.turno().name(), t.turno().nombre(), t.indicadores()))
                        .toList(),
                reporte.ranking());
    }

    public record Turno(String codigo, String nombre, Indicadores indicadores) {
    }
}
