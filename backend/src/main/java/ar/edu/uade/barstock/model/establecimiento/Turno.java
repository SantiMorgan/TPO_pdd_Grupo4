package ar.edu.uade.barstock.model.establecimiento;

/** Turnos de atención de una jornada. */
public enum Turno {
    MEDIODIA("Mediodía"), NOCHE("Noche");

    private final String nombre;

    Turno(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }
}
