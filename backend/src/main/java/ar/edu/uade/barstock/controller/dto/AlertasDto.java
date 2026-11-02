package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.establecimiento.ConfiguracionAlertas;
import ar.edu.uade.barstock.model.notificacion.TipoNotificacion;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record AlertasDto(
        @NotEmpty Set<TipoNotificacion> canales,
        @Size(max = 120) String email,
        @Size(max = 30) String telefono) {

    public static AlertasDto desde(ConfiguracionAlertas alertas) {
        return new AlertasDto(alertas.getCanales(), alertas.getEmail(), alertas.getTelefono());
    }

    public ConfiguracionAlertas aModelo() {
        return new ConfiguracionAlertas(canales, email, telefono);
    }
}
