package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de lectura: los reportes no dependen de cómo se guardan las comandas (variaciones protegidas).
 * Cada consulta devuelve lo ocurrido en [desde, hasta) en los establecimientos pedidos.
 */
public interface FuenteDatosReporte {
    List<Comanda> comandasDespachadas(Collection<UUID> establecimientos, Instant desde, Instant hasta);

    List<MovimientoStock> compras(Collection<UUID> establecimientos, Instant desde, Instant hasta);

    List<Auditoria> auditorias(Collection<UUID> establecimientos, Instant desde, Instant hasta);
}
