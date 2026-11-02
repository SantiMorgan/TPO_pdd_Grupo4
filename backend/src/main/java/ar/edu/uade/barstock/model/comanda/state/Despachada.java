package ar.edu.uade.barstock.model.comanda.state;

import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.ItemComanda;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

/**
 * Entregada y cobrada. Solo admite devoluciones por trago, que no reponen stock porque el trago
 * ya se elaboró. Cuando se devuelven todas las unidades, la comanda pasa a Anulada.
 */
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
        return EnumSet.of(AccionComanda.DEVOLVER);
    }

    @Override
    public void devolver(Comanda comanda, ItemComanda item, int cantidad, String motivo, Instant ahora) {
        comanda.registrarDevolucion(item, cantidad, motivo, ahora);
        if (comanda.todoDevuelto()) {
            comanda.registrarAnulacion(this, motivo, ahora);
            comanda.setEstado(EstadosComanda.ANULADA);
        }
    }
}
