package ar.edu.uade.barstock.model.notificacion;

import org.springframework.stereotype.Component;

/**
 * Simple Factory: el único lugar que conoce las clases concretas de notificación.
 * Es un bean (y no un método estático) porque los productos necesitan la bitácora para registrar el envío.
 * Agregar un canal nuevo toca este switch y no a quienes envían alertas.
 */
@Component
public class NotificacionFactory {
    private final BitacoraNotificaciones bitacora;

    public NotificacionFactory(BitacoraNotificaciones bitacora) {
        this.bitacora = bitacora;
    }

    public Notificacion crear(TipoNotificacion tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de notificación es obligatorio");
        }
        return switch (tipo) {
            case PANEL -> new NotificacionPanel(bitacora);
            case EMAIL -> new NotificacionEmail(bitacora);
            case SMS -> new NotificacionSms(bitacora);
            case WHATSAPP -> new NotificacionWhatsApp(bitacora);
        };
    }
}
