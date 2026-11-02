package ar.edu.uade.barstock.model;

import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import ar.edu.uade.barstock.model.receta.Receta;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Datos de ejemplo para las pruebas unitarias, con los valores del Bar Ámbar de la demo. */
public final class Ejemplos {
    public static final UUID BAR = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

    private Ejemplos() {
    }

    public static Insumo insumo(String codigo, String stock, String costoUnitario, String puntoReorden) {
        return new Insumo(BAR, codigo, codigo.charAt(0) + codigo.substring(1).toLowerCase(), UnidadMedida.ML,
                new BigDecimal(stock), new BigDecimal(costoUnitario), new BigDecimal(puntoReorden), 750);
    }

    public static Insumo gin() {
        return insumo("GIN", "900", "25.33", "300");
    }

    public static Insumo tonica() {
        return insumo("TONICA", "3000", "3.20", "800");
    }

    /** Gin Tonic: 50 ml de gin y 150 ml de tónica. Costo 1746,50 y precio base 9800. */
    public static Receta ginTonic(Insumo gin, Insumo tonica) {
        Map<Insumo, Integer> ingredientes = new LinkedHashMap<>();
        ingredientes.put(gin, 50);
        ingredientes.put(tonica, 150);
        return new Receta(BAR, "Gin Tonic", "Clásico", "🍸", new BigDecimal("9800"), ingredientes);
    }
}
