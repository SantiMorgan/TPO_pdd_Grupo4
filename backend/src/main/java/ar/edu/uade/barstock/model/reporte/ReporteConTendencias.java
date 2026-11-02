package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.comanda.Comanda;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Compara las ventas de la primera y la segunda mitad de los despachos, en orden cronológico.
 * Es una comparación exploratoria, no una predicción.
 */
public class ReporteConTendencias extends ReporteDecorator {
    public ReporteConTendencias(Reporte reporte, DatosReporte datos) {
        super(reporte, datos);
    }

    @Override
    protected SeccionReporte seccion() {
        List<Comanda> despachadas = datos.despachadas();
        int mitad = despachadas.size() / 2;
        List<Comanda> primera = despachadas.subList(0, mitad);
        List<Comanda> segunda = despachadas.subList(mitad, despachadas.size());
        BigDecimal ventasPrimera = ventas(primera);
        BigDecimal ventasSegunda = ventas(segunda);
        boolean comparable = !primera.isEmpty() && !segunda.isEmpty();
        BigDecimal variacion = comparable && ventasPrimera.signum() > 0
                ? ventasSegunda.subtract(ventasPrimera).multiply(new BigDecimal("100"))
                        .divide(ventasPrimera, 1, RoundingMode.HALF_UP)
                : null;
        return new SeccionReporte("TENDENCIAS", "Tendencia de ventas",
                new Tendencia(ventasPrimera, ventasSegunda, primera.size(), segunda.size(), variacion, comparable));
    }

    private static BigDecimal ventas(List<Comanda> comandas) {
        return ReporteBase.sumar(comandas.stream().filter(Comanda::cuentaComoVenta).map(Comanda::getTotal).toList());
    }

    public record Tendencia(BigDecimal primeraMitad, BigDecimal segundaMitad, int despachosPrimeraMitad,
                            int despachosSegundaMitad, BigDecimal variacionPorcentaje, boolean comparable) {
    }
}
