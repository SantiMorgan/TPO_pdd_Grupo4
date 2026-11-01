package ar.edu.uade.barstock.model.inventario;

import java.util.UUID;

public class InsumoNoEncontradoException extends RuntimeException {
    public InsumoNoEncontradoException(UUID id) {
        super("No existe el insumo " + id);
    }
}
