package ar.edu.uade.barstock.model.precio;

import ar.edu.uade.barstock.model.receta.Receta;
import java.math.BigDecimal;
import java.util.List;

/** Menú con el precio que corresponde ahora según la estrategia vigente. */
public record Carta(EstrategiaVigente vigente, List<Item> items) {
    public static Carta armar(EstrategiaVigente vigente, List<Receta> recetas) {
        EstrategiaPrecio estrategia = vigente.estrategia();
        return new Carta(vigente, recetas.stream()
                .map(receta -> new Item(receta, estrategia.calcularPrecio(receta), receta.calcularCosto(), receta.disponible()))
                .toList());
    }

    public record Item(Receta receta, BigDecimal precio, BigDecimal costo, boolean disponible) {
    }
}
