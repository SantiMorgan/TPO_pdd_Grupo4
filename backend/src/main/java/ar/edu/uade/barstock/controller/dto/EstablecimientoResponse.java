package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import java.util.UUID;

public record EstablecimientoResponse(UUID id, String codigo, String nombre, String zona, String descripcion,
                                      PoliticaPrecioDto politicaPrecio, AlertasDto alertas) {
    public static EstablecimientoResponse desde(Establecimiento establecimiento) {
        return new EstablecimientoResponse(establecimiento.getId(), establecimiento.getCodigo(),
                establecimiento.getNombre(), establecimiento.getZona(), establecimiento.getDescripcion(),
                PoliticaPrecioDto.desde(establecimiento.getPoliticaPrecio()),
                AlertasDto.desde(establecimiento.getAlertas()));
    }
}
