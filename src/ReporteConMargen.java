import java.util.List;

public class ReporteConMargen
        extends ReporteDecorator {

    private List<Venta> ventas;

    public ReporteConMargen(
            Reporte reporte,
            List<Venta> ventas) {

        super(reporte);

        this.ventas = ventas;
    }

    @Override
    public String generar() {

        double ganancia = 0;

        for (Venta venta : ventas) {
            ganancia +=
                    venta.calcularGanancia();
        }

        return reporte.generar()
                + "\nGanancia estimada: $"
                + String.format("%.2f", ganancia);
    }
}