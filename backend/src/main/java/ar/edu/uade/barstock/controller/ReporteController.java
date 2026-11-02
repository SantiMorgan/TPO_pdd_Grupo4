package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.dto.ReporteDiarioResponse;
import ar.edu.uade.barstock.controller.dto.ReporteResponse;
import ar.edu.uade.barstock.facade.BarStockFacade;
import ar.edu.uade.barstock.model.establecimiento.Turno;
import ar.edu.uade.barstock.model.reporte.TipoSeccion;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Las fechas son jornadas (de 6:00 a 5:59 del día siguiente). Sin {@code desde}/{@code hasta}
 * se reportan las últimas 7 jornadas y sin {@code turno}, los dos turnos.
 */
@RestController
@RequestMapping("/api")
public class ReporteController {
    private final BarStockFacade facade;

    public ReporteController(BarStockFacade facade) {
        this.facade = facade;
    }

    /** Ejemplo: {@code ?secciones=GRAFICO,TENDENCIAS&turno=NOCHE} envuelve el reporte base con esos dos decoradores. */
    @GetMapping("/establecimientos/{establecimientoId}/reportes")
    public ReporteResponse generar(@PathVariable UUID establecimientoId,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                   @RequestParam(required = false) Turno turno,
                                   @RequestParam(required = false) List<TipoSeccion> secciones) {
        return ReporteResponse.desde(facade.generarReporte(List.of(establecimientoId), desde, hasta, turno,
                secciones == null ? List.of() : secciones));
    }

    /** Varios establecimientos juntos. Sin {@code establecimientos} reporta todos. */
    @GetMapping("/reportes")
    public ReporteResponse generarConsolidado(@RequestParam(required = false) List<UUID> establecimientos,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                              @RequestParam(required = false) Turno turno,
                                              @RequestParam(required = false) List<TipoSeccion> secciones) {
        return ReporteResponse.desde(facade.generarReporte(establecimientos == null ? List.of() : establecimientos,
                desde, hasta, turno, secciones == null ? List.of() : secciones));
    }

    /** Reporte diario: mediodía, noche y el total de la jornada. Sin {@code jornada} usa la actual. */
    @GetMapping("/reportes/diario")
    public ReporteDiarioResponse diario(@RequestParam(required = false) List<UUID> establecimientos,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate jornada) {
        return ReporteDiarioResponse.desde(facade.generarReporteDiario(
                establecimientos == null ? List.of() : establecimientos, jornada));
    }
}
