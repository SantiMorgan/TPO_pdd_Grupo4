package ar.edu.uade.barstock.model.inventario;

import java.math.BigDecimal;
import java.util.UUID;

/** Lo que el Inventario (sujeto) informa a sus observadores después de cada cambio confirmado. */
public record EventoStock(UUID establecimientoId, UUID insumoId, String nombreInsumo, UnidadMedida unidad,
                          BigDecimal stockAnterior, BigDecimal stockActual, BigDecimal puntoReorden) {
    public static EventoStock de(Insumo insumo, BigDecimal stockAnterior) {
        return new EventoStock(insumo.getEstablecimientoId(), insumo.getId(), insumo.getNombre(),
                insumo.getUnidadMedida(), stockAnterior, insumo.getStockActual(), insumo.getPuntoReorden());
    }

    /** Solo el cruce hacia abajo dispara la alerta, para no repetirla con cada trago. */
    public boolean cruzoPuntoDeReposicion() {
        return stockAnterior.compareTo(puntoReorden) > 0 && stockActual.compareTo(puntoReorden) <= 0;
    }
}
