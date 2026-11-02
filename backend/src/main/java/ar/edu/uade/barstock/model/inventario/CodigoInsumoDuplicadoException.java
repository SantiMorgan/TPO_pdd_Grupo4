package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.exception.ReglaDeNegocioException;

public class CodigoInsumoDuplicadoException extends ReglaDeNegocioException {
    public CodigoInsumoDuplicadoException() {
        super("Ya existe un insumo con ese código en el establecimiento, incluso si está dado de baja");
    }
}
