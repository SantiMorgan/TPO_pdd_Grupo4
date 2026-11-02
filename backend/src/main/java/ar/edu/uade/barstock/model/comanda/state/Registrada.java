package ar.edu.uade.barstock.model.comanda.state;

import ar.edu.uade.barstock.model.comanda.Comanda;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

/** Recién tomada. Todavía no consumió stock: se puede preparar o anular sin efectos. */
public final class Registrada implements EstadoComanda {
    Registrada() {
    }

    @Override
    public String codigo() {
        return "REGISTRADA";
    }

    @Override
    public String nombre() {
        return "Registrada";
    }

    @Override
    public Set<AccionComanda> accionesPermitidas() {
        return EnumSet.of(AccionComanda.PREPARAR, AccionComanda.ANULAR);
    }

    @Override
    public boolean enCurso() {
        return true;
    }

    @Override
    public void preparar(Comanda comanda, Instant ahora) {
        comanda.registrarPreparacion(ahora);
        comanda.setEstado(EstadosComanda.EN_PREPARACION);
    }

    @Override
    public void anular(Comanda comanda, String motivo, Instant ahora) {
        comanda.registrarAnulacion(this, motivo, ahora);
        comanda.setEstado(EstadosComanda.ANULADA);
    }
}
