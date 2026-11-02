package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.exception.RecursoNoEncontradoException;
import java.util.UUID;

public class InsumoNoEncontradoException extends RecursoNoEncontradoException {
    public InsumoNoEncontradoException(UUID id) {
        super("No existe el insumo " + id);
    }
}
