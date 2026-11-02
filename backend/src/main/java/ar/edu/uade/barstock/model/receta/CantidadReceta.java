package ar.edu.uade.barstock.model.receta;

import java.util.UUID;

/** Cuántas unidades de una receta pide una comanda. */
public record CantidadReceta(UUID recetaId, int cantidad) {
}
