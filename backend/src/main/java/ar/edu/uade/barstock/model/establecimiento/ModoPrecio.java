package ar.edu.uade.barstock.model.establecimiento;

/** AUTOMATICO elige la estrategia según la hora y la demanda; los demás la fijan desde la configuración del dueño. */
public enum ModoPrecio {
    AUTOMATICO, NORMAL, HAPPY_HOUR, DEMANDA_ALTA
}
