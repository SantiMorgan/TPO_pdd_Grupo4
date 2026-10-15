public abstract class ReporteDecorator implements Reporte {

    protected Reporte reporte;

    public ReporteDecorator(Reporte reporte) {
        this.reporte = reporte;
    }
}
