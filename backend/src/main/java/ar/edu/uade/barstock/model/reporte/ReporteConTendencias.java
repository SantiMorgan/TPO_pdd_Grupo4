package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.ItemComanda;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Compara las ventas del período elegido con las del período anterior de la misma duración
 * (por ejemplo, del 1/11 al 7/11 contra del 25/10 al 31/10), con el mismo filtro de turno.
 * Desglosa por jornada, por establecimiento y por trago. Es una comparación, no una predicción.
 */
public class ReporteConTendencias extends ReporteDecorator {
    private static final int TRAGOS_DESTACADOS = 6;

    public ReporteConTendencias(Reporte reporte, DatosReporte datos) {
        super(reporte, datos);
    }

    @Override
    protected SeccionReporte seccion() {
        Periodo actual = datos.periodo();
        List<Comanda> ahora = datos.despachadas();
        List<Comanda> antes = datos.despachadasPeriodoAnterior();
        BigDecimal ventasActual = ventas(ahora);
        BigDecimal ventasAnterior = ventas(antes);

        List<VentaJornada> porJornada = actual.jornadas().stream().map(jornada -> {
            List<Comanda> delDia = ahora.stream()
                    .filter(c -> datos.calendario().jornadaDe(c.getDespachadaEn()).equals(jornada)).toList();
            return new VentaJornada(jornada, ventas(delDia), cobradas(delDia));
        }).toList();

        List<TendenciaEstablecimiento> porEstablecimiento = datos.establecimientos().stream().map(e -> {
            BigDecimal deAhora = ventas(delEstablecimiento(ahora, e.getId()));
            BigDecimal deAntes = ventas(delEstablecimiento(antes, e.getId()));
            return new TendenciaEstablecimiento(e.getId(), e.getNombre(), deAhora, deAntes, variacion(deAhora, deAntes));
        }).toList();

        return new SeccionReporte("TENDENCIAS", "Tendencia de ventas",
                new Tendencia(actual, actual.anterior(), ventasActual, ventasAnterior, cobradas(ahora), cobradas(antes),
                        variacion(ventasActual, ventasAnterior), porJornada, porEstablecimiento,
                        tragos(unidadesPorTrago(ahora), unidadesPorTrago(antes))));
    }

    private static List<TendenciaTrago> tragos(Map<String, Integer> ahora, Map<String, Integer> antes) {
        Map<String, int[]> todos = new LinkedHashMap<>();
        ahora.forEach((trago, unidades) -> todos.computeIfAbsent(trago, t -> new int[2])[0] = unidades);
        antes.forEach((trago, unidades) -> todos.computeIfAbsent(trago, t -> new int[2])[1] = unidades);
        return todos.entrySet().stream()
                .map(e -> new TendenciaTrago(e.getKey(), e.getValue()[0], e.getValue()[1],
                        variacion(BigDecimal.valueOf(e.getValue()[0]), BigDecimal.valueOf(e.getValue()[1]))))
                .sorted(Comparator.comparingInt((TendenciaTrago t) -> Math.abs(t.unidadesActual() - t.unidadesAnterior()))
                        .reversed().thenComparing(TendenciaTrago::trago))
                .limit(TRAGOS_DESTACADOS)
                .toList();
    }

    private static Map<String, Integer> unidadesPorTrago(List<Comanda> comandas) {
        Map<String, Integer> unidades = new LinkedHashMap<>();
        for (Comanda comanda : comandas) {
            if (comanda.cuentaComoVenta()) {
                for (ItemComanda item : comanda.getItems()) {
                    unidades.merge(item.getNombreReceta(), item.cantidadVendida(), Integer::sum);
                }
            }
        }
        return unidades;
    }

    private static List<Comanda> delEstablecimiento(List<Comanda> comandas, UUID establecimientoId) {
        return comandas.stream().filter(c -> c.perteneceA(establecimientoId)).toList();
    }

    private static BigDecimal ventas(List<Comanda> comandas) {
        return ReporteBase.sumar(comandas.stream().map(Comanda::ventaNeta).toList());
    }

    private static int cobradas(List<Comanda> comandas) {
        return (int) comandas.stream().filter(Comanda::cuentaComoVenta).count();
    }

    /** Nulo cuando no hubo ventas en el período anterior: no hay contra qué comparar. */
    static BigDecimal variacion(BigDecimal actual, BigDecimal anterior) {
        return anterior.signum() > 0
                ? actual.subtract(anterior).multiply(new BigDecimal("100")).divide(anterior, 1, RoundingMode.HALF_UP)
                : null;
    }

    public record Tendencia(Periodo periodo, Periodo periodoAnterior, BigDecimal ventasActual,
                            BigDecimal ventasAnterior, int comandasActual, int comandasAnterior,
                            BigDecimal variacionPorcentaje, List<VentaJornada> porJornada,
                            List<TendenciaEstablecimiento> porEstablecimiento, List<TendenciaTrago> porTrago) {
    }

    public record VentaJornada(LocalDate jornada, BigDecimal ventas, int comandas) {
    }

    public record TendenciaEstablecimiento(UUID id, String nombre, BigDecimal ventasActual, BigDecimal ventasAnterior,
                                           BigDecimal variacionPorcentaje) {
    }

    public record TendenciaTrago(String trago, int unidadesActual, int unidadesAnterior, BigDecimal variacionPorcentaje) {
    }
}
