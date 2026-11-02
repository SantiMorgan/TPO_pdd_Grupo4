package ar.edu.uade.barstock.model.reporte;

/** Sección agregada por un decorador. {@code datos} es un objeto de valor propio de cada sección. */
public record SeccionReporte(String tipo, String titulo, Object datos) {
}
