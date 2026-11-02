package ar.edu.uade.barstock.model.inventario;

import java.util.UUID;

/** El consumo pide una unidad distinta de la que usa el insumo. La API la traduce a HTTP 422. */
public class UnidadIncompatibleException extends RuntimeException {
    public UnidadIncompatibleException(UUID insumoId, UnidadMedida esperada, UnidadMedida recibida) {
        super("El insumo " + insumoId + " se mide en " + esperada + " y el consumo indica " + recibida);
    }
}
