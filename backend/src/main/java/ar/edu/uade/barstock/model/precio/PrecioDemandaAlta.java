package ar.edu.uade.barstock.model.precio;

import ar.edu.uade.barstock.model.receta.Receta;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Recargo porcentual cuando la barra está saturada de comandas. */
public class PrecioDemandaAlta implements EstrategiaPrecio {
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final BigDecimal incremento;

    public PrecioDemandaAlta(BigDecimal incrementoPorcentaje) {
        this.incremento = incrementoPorcentaje;
    }

    @Override
    public String codigo() {
        return "DEMANDA_ALTA";
    }

    @Override
    public String nombre() {
        return "Demanda alta +" + incremento.stripTrailingZeros().toPlainString() + " %";
    }

    @Override
    public BigDecimal calcularPrecio(Receta receta) {
        return receta.getPrecioBase().multiply(CIEN.add(incremento)).divide(CIEN, 2, RoundingMode.HALF_UP);
    }
}
