package ar.edu.uade.barstock.model.comanda.state;

import ar.edu.uade.barstock.model.comanda.Comanda;
import java.time.Instant;
import java.util.Set;

/**
 * State: cada estado decide qué operaciones admite y a qué estado pasa la comanda.
 * Por defecto toda transición es inválida; cada estado concreto habilita solo las suyas.
 */
public interface EstadoComanda {
    String codigo();

    String nombre();

    Set<AccionComanda> accionesPermitidas();

    default boolean enCurso() {
        return false;
    }

    default void preparar(Comanda comanda, Instant ahora) {
        throw new TransicionInvalidaException(comanda, this, "preparar");
    }

    default void despachar(Comanda comanda, Instant ahora) {
        throw new TransicionInvalidaException(comanda, this, "despachar");
    }

    default void anular(Comanda comanda, String motivo, Instant ahora) {
        throw new TransicionInvalidaException(comanda, this, "anular");
    }
}
