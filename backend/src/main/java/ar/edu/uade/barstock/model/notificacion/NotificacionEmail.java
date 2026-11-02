package ar.edu.uade.barstock.model.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Envío por email simulado: se registra y se loguea, sin conectarse a un servidor SMTP. */
public class NotificacionEmail implements Notificacion {
    private static final Logger log = LoggerFactory.getLogger(NotificacionEmail.class);
    private final BitacoraNotificaciones bitacora;

    NotificacionEmail(BitacoraNotificaciones bitacora) {
        this.bitacora = bitacora;
    }

    @Override
    public TipoNotificacion tipo() {
        return TipoNotificacion.EMAIL;
    }

    @Override
    public void enviar(MensajeAlerta mensaje) {
        log.info("Email simulado a {}: {}", mensaje.destinatario(), mensaje.texto());
        bitacora.registrar(mensaje, tipo(), ResultadoNotificacion.SIMULADA);
    }
}
