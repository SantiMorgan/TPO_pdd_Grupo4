package ar.edu.uade.barstock.model.inventario;

public class CodigoInsumoDuplicadoException extends RuntimeException {
    public CodigoInsumoDuplicadoException() {
        super("Ya existe un insumo con ese código, incluso si está dado de baja");
    }
}
