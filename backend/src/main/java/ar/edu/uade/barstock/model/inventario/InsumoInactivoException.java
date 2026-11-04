package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.exception.ReglaDeNegocioException;

public class InsumoInactivoException extends ReglaDeNegocioException {
    public InsumoInactivoException() {
        super("El insumo está inactivo; debe darse de alta antes de editarlo");
    }

    public InsumoInactivoException(String nombre) {
        super("El insumo " + nombre + " está dado de baja y no puede usarse hasta reactivarlo");
    }
}
