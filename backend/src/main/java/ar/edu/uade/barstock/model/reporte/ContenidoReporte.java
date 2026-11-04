package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.establecimiento.Turno;
import java.util.ArrayList;
import java.util.List;

/** Resultado de generar un reporte: indicadores base más una sección por cada decorador aplicado. */
public class ContenidoReporte {
    private final Indicadores indicadores;
    private final Periodo periodo;
    private final Turno turno;
    private final List<String> establecimientos;
    private final List<String> composicion = new ArrayList<>();
    private final List<SeccionReporte> secciones = new ArrayList<>();

    public ContenidoReporte(Indicadores indicadores, Periodo periodo, Turno turno, List<String> establecimientos) {
        this.indicadores = indicadores;
        this.periodo = periodo;
        this.turno = turno;
        this.establecimientos = List.copyOf(establecimientos);
        this.composicion.add(ReporteBase.class.getSimpleName());
    }

    void agregarSeccion(String decorador, SeccionReporte seccion) {
        composicion.add(decorador);
        secciones.add(seccion);
    }

    public Indicadores getIndicadores() { return indicadores; }
    public Periodo getPeriodo() { return periodo; }
    public Turno getTurno() { return turno; }
    public List<String> getEstablecimientos() { return establecimientos; }
    public List<String> getComposicion() { return List.copyOf(composicion); }
    public List<SeccionReporte> getSecciones() { return List.copyOf(secciones); }
}
