package ar.edu.uade.barstock.model.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Envío por WhatsApp simulado: no se contrata un proveedor real. */
public class NotificacionWhatsApp implements Notificacion {
    private static final Logger log = LoggerFactory.getLogger(NotificacionWhatsApp.class);
    private final BitacoraNotificaciones bitacora;

    NotificacionWhatsApp(BitacoraNotificaciones bitacora) {
        this.bitacora = bitacora;
    }

    @Override
    public TipoNotificacion tipo() {
        return TipoNotificacion.WHATSAPP;
    }

    @Override
    public void enviar(MensajeAlerta mensaje) {
        log.info("WhatsApp simulado a {}: {}", mensaje.destinatario(), mensaje.texto());
        bitacora.registrar(mensaje, tipo(), ResultadoNotificacion.SIMULADA);
    }
}
