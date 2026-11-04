package ar.edu.uade.barstock.model.inventario;

import java.math.BigDecimal;
import java.util.UUID;

/** Resultado de llevar un insumo a su recuento físico; lo usa la auditoría para valorizar la merma. */
public record AjusteStock(UUID insumoId, String nombre, UnidadMedida unidad, int contenidoEnvase,
                          BigDecimal stockTeorico, BigDecimal stockFisico, BigDecimal costoUnitario, String comentario) {
    public BigDecimal diferencia() {
        return stockFisico.subtract(stockTeorico);
    }
}
