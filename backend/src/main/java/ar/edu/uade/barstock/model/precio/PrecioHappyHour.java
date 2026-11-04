package ar.edu.uade.barstock.model.precio;

import ar.edu.uade.barstock.model.receta.Receta;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Descuento porcentual sobre el precio base, con un piso: el precio nunca queda por debajo de
 * costo × (1 + margen mínimo). Ejemplo con 20 % y margen 50 %: base 9800 y costo 1746,50 → 7840.
 */
public class PrecioHappyHour implements EstrategiaPrecio {
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final BigDecimal descuento;
    private final BigDecimal margenMinimo;

    public PrecioHappyHour(BigDecimal descuentoPorcentaje, BigDecimal margenMinimoPorcentaje) {
        this.descuento = descuentoPorcentaje;
        this.margenMinimo = margenMinimoPorcentaje;
    }

    @Override
    public String codigo() {
        return "HAPPY_HOUR";
    }

    @Override
    public String nombre() {
        return "Happy Hour −" + descuento.stripTrailingZeros().toPlainString() + " %";
    }

    @Override
    public BigDecimal calcularPrecio(Receta receta) {
        BigDecimal base = receta.getPrecioBase();
        BigDecimal conDescuento = base.multiply(CIEN.subtract(descuento)).divide(CIEN, 2, RoundingMode.HALF_UP);
        BigDecimal piso = receta.calcularCosto().multiply(CIEN.add(margenMinimo)).divide(CIEN, 2, RoundingMode.HALF_UP);
        return conDescuento.max(piso).min(base);
    }

    /** Para mostrar si el piso de margen limitó el descuento. */
    public boolean limitadoPorMargen(Receta receta) {
        BigDecimal conDescuento = receta.getPrecioBase().multiply(CIEN.subtract(descuento)).divide(CIEN, 2, RoundingMode.HALF_UP);
        return calcularPrecio(receta).compareTo(conDescuento) > 0;
    }
}
