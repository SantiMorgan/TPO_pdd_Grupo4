package ar.edu.uade.barstock.model.inventario;

public class InsumoInactivoException extends RuntimeException {
    public InsumoInactivoException() {
        super("El insumo está inactivo; debe darse de alta antes de editarlo");
    }
}
