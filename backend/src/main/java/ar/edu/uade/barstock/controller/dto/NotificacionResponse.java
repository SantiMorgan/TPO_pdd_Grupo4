package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.notificacion.RegistroNotificacion;
import ar.edu.uade.barstock.model.notificacion.ResultadoNotificacion;
import ar.edu.uade.barstock.model.notificacion.TipoNotificacion;
import java.time.Instant;
import java.util.UUID;

public record NotificacionResponse(UUID id, UUID insumoId, TipoNotificacion canal, String destinatario,
                                   String mensaje, ResultadoNotificacion resultado, Instant fecha) {
    public static NotificacionResponse desde(RegistroNotificacion registro) {
        return new NotificacionResponse(registro.getId(), registro.getInsumoId(), registro.getCanal(),
                registro.getDestinatario(), registro.getMensaje(), registro.getResultado(), registro.getFecha());
    }
}
