package ar.edu.uade.barstock.model.reporte;

import java.util.UUID;

/** Puerto de lectura: los reportes no dependen de cómo se guardan las comandas (variaciones protegidas). */
public interface FuenteDatosReporte {
    DatosReporte obtener(UUID establecimientoId);
}
