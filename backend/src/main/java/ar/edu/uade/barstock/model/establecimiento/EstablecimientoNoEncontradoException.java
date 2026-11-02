package ar.edu.uade.barstock.model.establecimiento;

import ar.edu.uade.barstock.model.exception.RecursoNoEncontradoException;
import java.util.UUID;

public class EstablecimientoNoEncontradoException extends RecursoNoEncontradoException {
    public EstablecimientoNoEncontradoException(UUID id) {
        super("No existe el establecimiento " + id);
    }
}
