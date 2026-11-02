package ar.edu.uade.barstock.model.inventario;

import java.util.Objects;
import java.util.UUID;
import java.math.BigDecimal;

/** Cantidad de un insumo que consume una preparación, siempre con su unidad. */
public record Consumo(UUID insumoId, BigDecimal cantidad, UnidadMedida unidad) {
    public Consumo {
        Objects.requireNonNull(insumoId, "insumoId");
        Objects.requireNonNull(unidad, "unidad");
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new IllegalArgumentException("La cantidad consumida debe ser mayor a cero");
        }
    }

    public Consumo sumar(Consumo otro) {
        if (otro.unidad != unidad) {
            throw new UnidadIncompatibleException(insumoId, unidad, otro.unidad);
        }
        return new Consumo(insumoId, cantidad.add(otro.cantidad), unidad);
    }
}
