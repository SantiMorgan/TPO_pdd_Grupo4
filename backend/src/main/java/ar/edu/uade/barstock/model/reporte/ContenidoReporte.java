package ar.edu.uade.barstock.model.reporte;

import java.util.ArrayList;
import java.util.List;

/** Resultado de generar un reporte: indicadores base más una sección por cada decorador aplicado. */
public class ContenidoReporte {
    private final Indicadores indicadores;
    private final List<String> composicion = new ArrayList<>();
    private final List<SeccionReporte> secciones = new ArrayList<>();

    public ContenidoReporte(Indicadores indicadores) {
        this.indicadores = indicadores;
        this.composicion.add(ReporteBase.class.getSimpleName());
    }

    void agregarSeccion(String decorador, SeccionReporte seccion) {
        composicion.add(decorador);
        secciones.add(seccion);
    }

    public Indicadores getIndicadores() { return indicadores; }
    public List<String> getComposicion() { return List.copyOf(composicion); }
    public List<SeccionReporte> getSecciones() { return List.copyOf(secciones); }
}
