package ar.edu.uade.barstock.model.comanda.state;

import ar.edu.uade.barstock.model.comanda.Comanda;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

/** El bartender la está preparando. Despacharla es lo que descuenta el stock (en la Facade). */
public final class EnPreparacion implements EstadoComanda {
    EnPreparacion() {
    }

    @Override
    public String codigo() {
        return "EN_PREPARACION";
    }

    @Override
    public String nombre() {
        return "En preparación";
    }

    @Override
    public Set<AccionComanda> accionesPermitidas() {
        return EnumSet.of(AccionComanda.DESPACHAR, AccionComanda.ANULAR);
    }

    @Override
    public boolean enCurso() {
        return true;
    }

    @Override
    public void despachar(Comanda comanda, Instant ahora) {
        comanda.registrarDespacho(ahora);
        comanda.setEstado(EstadosComanda.DESPACHADA);
    }

    /** Preparación fallida: todavía no se descontó nada, no hay que reponer stock. */
    @Override
    public void anular(Comanda comanda, String motivo, Instant ahora) {
        comanda.registrarAnulacion(this, motivo, ahora);
        comanda.setEstado(EstadosComanda.ANULADA);
    }
}
