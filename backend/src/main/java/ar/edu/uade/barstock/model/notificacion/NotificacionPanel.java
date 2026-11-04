package ar.edu.uade.barstock.model.notificacion;

/** Aviso visible en el panel de alertas de la aplicación. */
public class NotificacionPanel implements Notificacion {
    private final BitacoraNotificaciones bitacora;

    NotificacionPanel(BitacoraNotificaciones bitacora) {
        this.bitacora = bitacora;
    }

    @Override
    public TipoNotificacion tipo() {
        return TipoNotificacion.PANEL;
    }

    @Override
    public void enviar(MensajeAlerta mensaje) {
        bitacora.registrar(mensaje, tipo(), ResultadoNotificacion.MOSTRADA);
    }
}
