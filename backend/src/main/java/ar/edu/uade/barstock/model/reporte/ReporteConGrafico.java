package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.ItemComanda;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Agrega el ranking de tragos vendidos, listo para dibujar un gráfico de barras. */
public class ReporteConGrafico extends ReporteDecorator {
    public ReporteConGrafico(Reporte reporte, DatosReporte datos) {
        super(reporte, datos);
    }

    @Override
    protected SeccionReporte seccion() {
        Map<String, Acumulado> porTrago = new LinkedHashMap<>();
        for (Comanda comanda : datos.vendidas()) {
            for (ItemComanda item : comanda.getItems()) {
                porTrago.computeIfAbsent(item.getNombreReceta(), nombre -> new Acumulado()).sumar(item);
            }
        }
        int totalUnidades = porTrago.values().stream().mapToInt(a -> a.unidades).sum();
        List<Barra> barras = porTrago.entrySet().stream()
                .map(e -> new Barra(e.getKey(), e.getValue().unidades, e.getValue().ingresos.setScale(2),
                        BigDecimal.valueOf(e.getValue().unidades * 100L)
                                .divide(BigDecimal.valueOf(Math.max(1, totalUnidades)), 1, RoundingMode.HALF_UP)))
                .sorted(Comparator.comparingInt(Barra::unidades).reversed().thenComparing(Barra::trago))
                .toList();
        return new SeccionReporte("GRAFICO", "Ranking de tragos vendidos", barras);
    }

    public record Barra(String trago, int unidades, BigDecimal ingresos, BigDecimal participacion) {
    }

    private static final class Acumulado {
        private int unidades;
        private BigDecimal ingresos = BigDecimal.ZERO;

        void sumar(ItemComanda item) {
            unidades += item.getCantidad();
            ingresos = ingresos.add(item.subtotal());
        }
    }
}
