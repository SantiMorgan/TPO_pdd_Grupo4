package ar.edu.uade.barstock.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta y edición de un establecimiento. El código se ignora al editar. */
public record EstablecimientoRequest(
        @Size(max = 30) String codigo,
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String zona,
        @Size(max = 150) String descripcion) {
}
