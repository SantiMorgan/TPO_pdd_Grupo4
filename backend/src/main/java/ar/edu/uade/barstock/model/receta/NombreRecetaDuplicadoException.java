package ar.edu.uade.barstock.model.receta;

import ar.edu.uade.barstock.model.exception.ReglaDeNegocioException;

public class NombreRecetaDuplicadoException extends ReglaDeNegocioException {
    public NombreRecetaDuplicadoException(String nombre) {
        super("Ya existe una receta llamada " + nombre + " en el establecimiento");
    }
}
