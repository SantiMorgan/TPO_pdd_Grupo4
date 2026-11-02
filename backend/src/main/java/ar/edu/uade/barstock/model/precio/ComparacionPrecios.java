package ar.edu.uade.barstock.model.precio;

import ar.edu.uade.barstock.model.receta.Receta;
import java.math.BigDecimal;
import java.util.List;

/** Aplica cada estrategia a cada receta: muestra que son intercambiables sobre los mismos datos. */
public record ComparacionPrecios(List<EstrategiaPrecio> estrategias, List<Fila> filas) {
    public static ComparacionPrecios de(List<EstrategiaPrecio> estrategias, List<Receta> recetas) {
        return new ComparacionPrecios(estrategias, recetas.stream()
                .map(receta -> new Fila(receta, receta.calcularCosto(),
                        estrategias.stream().map(e -> e.calcularPrecio(receta)).toList()))
                .toList());
    }

    /** Los precios siguen el orden de la lista de estrategias. */
    public record Fila(Receta receta, BigDecimal costo, List<BigDecimal> precios) {
    }
}
