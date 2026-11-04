package ar.edu.uade.barstock.model.exception;

/** Recurso de dominio inexistente o de otro establecimiento. La API lo traduce a HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
