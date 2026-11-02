package ar.edu.uade.barstock.model.comanda.state;

import ar.edu.uade.barstock.model.comanda.Comanda;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

/** Entregada y cobrada. Solo admite la devolución, que no repone stock porque el trago ya se elaboró. */
public final class Despachada implements EstadoComanda {
    Despachada() {
    }

    @Override
    public String codigo() {
        return "DESPACHADA";
    }

    @Override
    public String nombre() {
        return "Despachada";
    }

    @Override
    public Set<AccionComanda> accionesPermitidas() {
        return EnumSet.of(AccionComanda.ANULAR);
    }

    @Override
    public void anular(Comanda comanda, String motivo, Instant ahora) {
        comanda.registrarAnulacion(this, motivo, ahora);
        comanda.setEstado(EstadosComanda.ANULADA);
    }
}
