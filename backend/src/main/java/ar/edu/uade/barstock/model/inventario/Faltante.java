package ar.edu.uade.barstock.model.inventario;

import java.math.BigDecimal;
import java.util.UUID;

public record Faltante(UUID insumoId, String insumo, UnidadMedida unidad, BigDecimal requerido, BigDecimal disponible) {
    public static Faltante de(Insumo insumo, BigDecimal requerido) {
        return new Faltante(insumo.getId(), insumo.getNombre(), insumo.getUnidadMedida(),
                requerido, insumo.getStockActual());
    }

    public BigDecimal faltan() {
        return requerido.subtract(disponible);
    }

    public String describir() {
        return insumo + ": faltan " + faltan().toPlainString() + " " + unidad.simbolo();
    }
}
