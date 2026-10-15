import java.util.List;

public class ReporteBasico
        implements Reporte {

    private List<Venta> ventas;

    public ReporteBasico(List<Venta> ventas) {
        this.ventas = ventas;
    }

    @Override
    public String generar() {

        double total = 0;

        for (Venta venta : ventas) {
            total += venta.getTotal();
        }

        return """
                ===== REPORTE BARSTOCK =====
                Cantidad de ventas: %d
                Facturación total: $%.2f
                """.formatted(
                ventas.size(),
                total
        );
    }
}