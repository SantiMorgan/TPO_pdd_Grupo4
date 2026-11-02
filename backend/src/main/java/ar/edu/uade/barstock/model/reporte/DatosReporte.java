package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import java.util.List;

/**
 * Datos del establecimiento que leen el reporte base y sus decoradores.
 * {@code despachadas} incluye las comandas devueltas después del despacho, ordenadas por fecha de despacho.
 */
public record DatosReporte(List<Comanda> despachadas, List<MovimientoStock> compras, List<Auditoria> auditorias) {
    public List<Comanda> vendidas() {
        return despachadas.stream().filter(Comanda::cuentaComoVenta).toList();
    }
}
