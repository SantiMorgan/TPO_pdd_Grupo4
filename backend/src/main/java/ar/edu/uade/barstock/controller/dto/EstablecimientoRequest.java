package ar.edu.uade.barstock.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta y edición de un establecimiento. El código se ignora al editar; sin horario se usa el actual o el común. */
public record EstablecimientoRequest(
        @Size(max = 30) String codigo,
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String zona,
        @NotBlank @Size(max = 120) String direccion,
        @Size(max = 150) String descripcion,
        @Valid HorarioDto horario) {
}
