package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.precio.ComparacionPrecios;
import ar.edu.uade.barstock.model.precio.EstrategiaPrecio;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ComparacionPreciosResponse(List<Estrategia> estrategias, List<Fila> filas) {
    public static ComparacionPreciosResponse desde(ComparacionPrecios comparacion) {
        List<Estrategia> estrategias = comparacion.estrategias().stream().map(Estrategia::de).toList();
        return new ComparacionPreciosResponse(estrategias, comparacion.filas().stream()
                .map(fila -> new Fila(fila.receta().getId(), fila.receta().getNombre(), fila.receta().getIcono(),
                        fila.receta().getPrecioBase(), fila.costo(), fila.precios()))
                .toList());
    }

    public record Estrategia(String codigo, String nombre, String clase) {
        static Estrategia de(EstrategiaPrecio estrategia) {
            return new Estrategia(estrategia.codigo(), estrategia.nombre(), estrategia.getClass().getSimpleName());
        }
    }

    /** {@code precios} sigue el orden de {@code estrategias}. */
    public record Fila(UUID recetaId, String nombre, String icono, BigDecimal precioBase, BigDecimal costo,
                       List<BigDecimal> precios) {
    }
}
