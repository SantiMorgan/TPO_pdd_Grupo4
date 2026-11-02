package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.traza.TrazaPatrones;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Arma la cadena de decoradores en tiempo de ejecución según las secciones que pide el dueño. */
@Service
public class GeneradorReportes {
    private final FuenteDatosReporte fuente;
    private final TrazaPatrones traza;

    public GeneradorReportes(FuenteDatosReporte fuente, TrazaPatrones traza) {
        this.fuente = fuente;
        this.traza = traza;
    }

    @Transactional(readOnly = true)
    public ContenidoReporte generar(UUID establecimientoId, Collection<TipoSeccion> secciones) {
        DatosReporte datos = fuente.obtener(establecimientoId);
        Reporte reporte = new ReporteBase(datos);
        for (TipoSeccion seccion : new LinkedHashSet<>(secciones)) {
            reporte = seccion.decorar(reporte, datos);
        }
        ContenidoReporte contenido = reporte.generar();
        if (!secciones.isEmpty()) {
            traza.registrar(establecimientoId, "Decorator", String.join(" + ", contenido.getComposicion()));
        }
        return contenido;
    }
}
