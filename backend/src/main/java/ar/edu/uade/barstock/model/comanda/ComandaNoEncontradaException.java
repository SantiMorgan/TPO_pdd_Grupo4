package ar.edu.uade.barstock.model.comanda;

import ar.edu.uade.barstock.model.exception.RecursoNoEncontradoException;
import java.util.UUID;

public class ComandaNoEncontradaException extends RecursoNoEncontradoException {
    public ComandaNoEncontradaException(UUID id) {
        super("No existe la comanda " + id);
    }
}
