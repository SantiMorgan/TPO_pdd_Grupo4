package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.dto.ReporteResponse;
import ar.edu.uade.barstock.facade.BarStockFacade;
import ar.edu.uade.barstock.model.reporte.TipoSeccion;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/establecimientos/{establecimientoId}/reportes")
public class ReporteController {
    private final BarStockFacade facade;

    public ReporteController(BarStockFacade facade) {
        this.facade = facade;
    }

    /** Ejemplo: {@code ?secciones=GRAFICO,TENDENCIAS} envuelve el reporte base con esos dos decoradores. */
    @GetMapping
    public ReporteResponse generar(@PathVariable UUID establecimientoId,
                                   @RequestParam(required = false) List<TipoSeccion> secciones) {
        return ReporteResponse.desde(facade.generarReporte(establecimientoId, secciones == null ? List.of() : secciones));
    }
}
