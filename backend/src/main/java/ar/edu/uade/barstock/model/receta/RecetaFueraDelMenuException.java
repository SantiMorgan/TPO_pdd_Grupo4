package ar.edu.uade.barstock.model.receta;

import ar.edu.uade.barstock.model.exception.ReglaDeNegocioException;

public class RecetaFueraDelMenuException extends ReglaDeNegocioException {
    public RecetaFueraDelMenuException(String nombre) {
        super(nombre + " no está en el menú en este momento");
    }
}
