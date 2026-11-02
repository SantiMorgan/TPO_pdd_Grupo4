package ar.edu.uade.barstock.model.comanda.state;

import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.exception.ReglaDeNegocioException;

public class TransicionInvalidaException extends ReglaDeNegocioException {
    public TransicionInvalidaException(Comanda comanda, EstadoComanda estado, String accion) {
        super("La comanda #" + comanda.getNumero() + " está " + estado.nombre() + ": no se puede " + accion);
    }
}
