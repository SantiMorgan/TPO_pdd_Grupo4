package ar.edu.uade.barstock.model.notificacion;

/** Producto de NotificacionFactory. Quien la usa no conoce la clase concreta (variaciones protegidas). */
public interface Notificacion {
    TipoNotificacion tipo();

    void enviar(MensajeAlerta mensaje);
}
