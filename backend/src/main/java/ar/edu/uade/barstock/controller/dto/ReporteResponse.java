package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.establecimiento.Turno;
import ar.edu.uade.barstock.model.reporte.ContenidoReporte;
import ar.edu.uade.barstock.model.reporte.Indicadores;
import ar.edu.uade.barstock.model.reporte.Periodo;
import ar.edu.uade.barstock.model.reporte.SeccionReporte;
import java.util.List;

/**
 * {@code composicion} lista la cadena de decoradores aplicada, empezando por ReporteBase.
 * {@code turno} nulo significa los dos turnos. Los indicadores y las secciones son objetos de valor.
 */
public record ReporteResponse(Periodo periodo, Periodo periodoAnterior, Turno turno, List<String> establecimientos,
                              List<String> composicion, Indicadores indicadores, List<SeccionReporte> secciones) {
    public static ReporteResponse desde(ContenidoReporte contenido) {
        return new ReporteResponse(contenido.getPeriodo(), contenido.getPeriodo().anterior(), contenido.getTurno(),
                contenido.getEstablecimientos(), contenido.getComposicion(), contenido.getIndicadores(),
                contenido.getSecciones());
    }
}
