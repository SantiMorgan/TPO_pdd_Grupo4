package ar.edu.uade.barstock.model.comanda.state;

import java.util.EnumSet;
import java.util.Set;

/** Estado final: rechaza cualquier operación con los métodos por defecto de la interfaz. */
public final class Anulada implements EstadoComanda {
    Anulada() {
    }

    @Override
    public String codigo() {
        return "ANULADA";
    }

    @Override
    public String nombre() {
        return "Anulada";
    }

    @Override
    public Set<AccionComanda> accionesPermitidas() {
        return EnumSet.noneOf(AccionComanda.class);
    }
}
