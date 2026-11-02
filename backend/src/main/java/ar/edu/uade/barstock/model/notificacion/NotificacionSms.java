package ar.edu.uade.barstock.model.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Envío por SMS simulado: no se contrata un proveedor real. */
public class NotificacionSms implements Notificacion {
    private static final Logger log = LoggerFactory.getLogger(NotificacionSms.class);
    private final BitacoraNotificaciones bitacora;

    NotificacionSms(BitacoraNotificaciones bitacora) {
        this.bitacora = bitacora;
    }

    @Override
    public TipoNotificacion tipo() {
        return TipoNotificacion.SMS;
    }

    @Override
    public void enviar(MensajeAlerta mensaje) {
        log.info("SMS simulado a {}: {}", mensaje.destinatario(), mensaje.texto());
        bitacora.registrar(mensaje, tipo(), ResultadoNotificacion.SIMULADA);
    }
}
