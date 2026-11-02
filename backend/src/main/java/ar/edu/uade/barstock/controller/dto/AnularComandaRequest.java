package ar.edu.uade.barstock.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnularComandaRequest(@NotBlank @Size(min = 5, max = 220) String motivo) {
}
