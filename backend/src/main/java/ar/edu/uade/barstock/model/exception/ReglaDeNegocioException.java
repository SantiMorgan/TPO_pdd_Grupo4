package ar.edu.uade.barstock.model.exception;

/** La operación no es válida en el estado actual del dominio. La API la traduce a HTTP 409. */
public class ReglaDeNegocioException extends RuntimeException {
    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
