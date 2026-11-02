package ar.edu.uade.barstock.model.reporte;

import java.util.Objects;

/**
 * Decorador abstracto: envuelve otro Reporte (base o ya decorado), genera su contenido
 * y le agrega una sección. Los decoradores se combinan en cualquier orden y cantidad.
 */
public abstract class ReporteDecorator implements Reporte {
    protected final Reporte reporte;
    protected final DatosReporte datos;

    protected ReporteDecorator(Reporte reporte, DatosReporte datos) {
        this.reporte = Objects.requireNonNull(reporte, "reporte");
        this.datos = Objects.requireNonNull(datos, "datos");
    }

    @Override
    public ContenidoReporte generar() {
        ContenidoReporte contenido = reporte.generar();
        contenido.agregarSeccion(getClass().getSimpleName(), seccion());
        return contenido;
    }

    protected abstract SeccionReporte seccion();
}
