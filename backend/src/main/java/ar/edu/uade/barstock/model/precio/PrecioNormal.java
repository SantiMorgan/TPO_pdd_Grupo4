package ar.edu.uade.barstock.model.precio;

import ar.edu.uade.barstock.model.receta.Receta;
import java.math.BigDecimal;

public class PrecioNormal implements EstrategiaPrecio {
    @Override
    public String codigo() {
        return "NORMAL";
    }

    @Override
    public String nombre() {
        return "Precio normal";
    }

    @Override
    public BigDecimal calcularPrecio(Receta receta) {
        return receta.getPrecioBase();
    }
}
