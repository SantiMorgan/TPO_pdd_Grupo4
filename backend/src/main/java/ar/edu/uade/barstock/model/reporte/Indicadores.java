package ar.edu.uade.barstock.model.reporte;

import java.math.BigDecimal;

/**
 * Indicadores del reporte base. Las ventas netas descuentan los tragos devueltos. La contribución es
 * ventas netas menos el costo de los insumos consumidos (incluidos los tragos devueltos); no es la
 * utilidad del negocio.
 */
public record Indicadores(BigDecimal ventasNetas, int comandasCobradas, int unidadesVendidas,
                          BigDecimal ticketPromedio, BigDecimal costoInsumos, BigDecimal contribucion,
                          BigDecimal margenContribucion, BigDecimal compras, BigDecimal mermas,
                          BigDecimal devoluciones, int tragosDevueltos, int auditorias) {
}
