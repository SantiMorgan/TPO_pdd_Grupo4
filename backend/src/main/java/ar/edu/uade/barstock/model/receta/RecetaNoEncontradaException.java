package ar.edu.uade.barstock.model.receta;

import ar.edu.uade.barstock.model.exception.RecursoNoEncontradoException;
import java.util.UUID;

public class RecetaNoEncontradaException extends RecursoNoEncontradoException {
    public RecetaNoEncontradaException(UUID id) {
        super("No existe la receta " + id);
    }
}
