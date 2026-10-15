public class AlertaStock implements StockObserver {

    private String destinatario;
    private String tipoNotificacion;

    public AlertaStock(
            String destinatario,
            String tipoNotificacion) {

        this.destinatario = destinatario;
        this.tipoNotificacion = tipoNotificacion;
    }

    @Override
    public void actualizar(Insumo insumo) {

        if (insumo.necesitaReposicion()) {

            MedioNotificacion medio = NotificacionFactory.crear(tipoNotificacion);

            medio.enviar(destinatario, "Stock bajo de " + insumo.getNombre() + ". Stock actual: " + insumo.getStockMl() + " ml.");
        }
    }
}
