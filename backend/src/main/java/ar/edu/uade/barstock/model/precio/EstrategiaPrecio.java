package ar.edu.uade.barstock.model.precio;

import ar.edu.uade.barstock.model.receta.Receta;
import java.math.BigDecimal;

/** Strategy: cada política de precios encapsula su algoritmo y es intercambiable en ejecución. */
public interface EstrategiaPrecio {
    /** Identificador estable para la API: NORMAL, HAPPY_HOUR o DEMANDA_ALTA. */
    String codigo();

    /** Texto para mostrar, con sus parámetros (por ejemplo, "Happy Hour −20 %"). */
    String nombre();

    /** Precio unitario de la receta, con dos decimales. */
    BigDecimal calcularPrecio(Receta receta);
}
