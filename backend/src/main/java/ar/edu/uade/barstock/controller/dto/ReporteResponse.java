package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.reporte.ContenidoReporte;
import ar.edu.uade.barstock.model.reporte.Indicadores;
import ar.edu.uade.barstock.model.reporte.SeccionReporte;
import java.util.List;

/**
 * {@code composicion} lista la cadena de decoradores aplicada, empezando por ReporteBase.
 * Los indicadores y las secciones son objetos de valor sin identidad, por eso se exponen tal cual.
 */
public record ReporteResponse(List<String> composicion, Indicadores indicadores, List<SeccionReporte> secciones) {
    public static ReporteResponse desde(ContenidoReporte contenido) {
        return new ReporteResponse(contenido.getComposicion(), contenido.getIndicadores(), contenido.getSecciones());
    }
}
