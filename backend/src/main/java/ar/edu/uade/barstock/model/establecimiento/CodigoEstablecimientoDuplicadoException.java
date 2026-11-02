package ar.edu.uade.barstock.model.establecimiento;

import ar.edu.uade.barstock.model.exception.ReglaDeNegocioException;

public class CodigoEstablecimientoDuplicadoException extends ReglaDeNegocioException {
    public CodigoEstablecimientoDuplicadoException() {
        super("Ya existe un establecimiento con ese código");
    }
}
