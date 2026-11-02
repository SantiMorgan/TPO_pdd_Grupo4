package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.ItemComanda;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Agrega la contribución de cada trago: ingresos cobrados menos el costo teórico de sus insumos. */
public class ReporteConMargen extends ReporteDecorator {
    public ReporteConMargen(Reporte reporte, DatosReporte datos) {
        super(reporte, datos);
    }

    @Override
    protected SeccionReporte seccion() {
        Map<String, BigDecimal[]> porTrago = new LinkedHashMap<>();
        Map<String, Integer> unidades = new LinkedHashMap<>();
        for (Comanda comanda : datos.vendidas()) {
            for (ItemComanda item : comanda.getItems()) {
                BigDecimal[] acumulado = porTrago.computeIfAbsent(item.getNombreReceta(),
                        nombre -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                acumulado[0] = acumulado[0].add(item.subtotal());
                acumulado[1] = acumulado[1].add(item.costo());
                unidades.merge(item.getNombreReceta(), item.getCantidad(), Integer::sum);
            }
        }
        List<MargenTrago> margenes = porTrago.entrySet().stream().map(e -> {
            BigDecimal ingresos = e.getValue()[0].setScale(2, RoundingMode.HALF_UP);
            BigDecimal costo = e.getValue()[1].setScale(2, RoundingMode.HALF_UP);
            BigDecimal contribucion = ingresos.subtract(costo);
            BigDecimal margen = ingresos.signum() == 0 ? null
                    : contribucion.multiply(new BigDecimal("100")).divide(ingresos, 1, RoundingMode.HALF_UP);
            return new MargenTrago(e.getKey(), unidades.get(e.getKey()), ingresos, costo, contribucion, margen);
        }).sorted(Comparator.comparing(MargenTrago::contribucion).reversed()).toList();
        return new SeccionReporte("MARGEN", "Margen por trago", margenes);
    }

    public record MargenTrago(String trago, int unidades, BigDecimal ingresos, BigDecimal costo,
                              BigDecimal contribucion, BigDecimal margenPorcentaje) {
    }
}
