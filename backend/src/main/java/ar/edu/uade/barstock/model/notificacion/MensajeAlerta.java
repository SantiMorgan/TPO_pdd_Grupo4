package ar.edu.uade.barstock.model.notificacion;

import java.util.UUID;

public record MensajeAlerta(UUID establecimientoId, UUID insumoId, String destinatario, String texto) {
}
