package ar.edu.uade.barstock.model.reporte;

import java.math.BigDecimal;

/**
 * Indicadores del reporte base. La contribución es ventas netas menos el costo de los insumos
 * consumidos (incluidas las devoluciones); no es la utilidad del negocio.
 */
public record Indicadores(BigDecimal ventasNetas, int comandasCobradas, int unidadesVendidas,
                          BigDecimal ticketPromedio, BigDecimal costoInsumos, BigDecimal contribucion,
                          BigDecimal margenContribucion, BigDecimal compras, BigDecimal mermas,
                          BigDecimal devoluciones, int comandasDevueltas, int auditorias) {
}
