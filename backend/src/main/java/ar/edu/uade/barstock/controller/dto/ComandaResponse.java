package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.OrigenComanda;
import ar.edu.uade.barstock.model.comanda.state.AccionComanda;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * {@code acciones} sale del estado actual (State): la interfaz muestra solo los botones válidos.
 * {@code total} es lo registrado; {@code totalNeto} descuenta los tragos devueltos.
 */
public record ComandaResponse(UUID id, int numero, String estado, String estadoNombre, Set<AccionComanda> acciones,
                              OrigenComanda origen, String cliente, String estrategiaPrecio, BigDecimal total,
                              BigDecimal totalDevuelto, BigDecimal totalNeto, BigDecimal costoTotal, List<Item> items,
                              List<Devolucion> devoluciones, Instant registradaEn, Instant preparadaEn,
                              Instant despachadaEn, Instant anuladaEn, String estadoAlAnular, String motivoAnulacion) {
    public static ComandaResponse desde(Comanda comanda) {
        BigDecimal devuelto = comanda.importeDevuelto();
        return new ComandaResponse(comanda.getId(), comanda.getNumero(), comanda.getEstado().codigo(),
                comanda.getEstado().nombre(), comanda.accionesPermitidas(), comanda.getOrigen(), comanda.getCliente(),
                comanda.getEstrategiaPrecio(), comanda.getTotal(), devuelto, comanda.getTotal().subtract(devuelto),
                comanda.getCostoTotal(),
                comanda.getItems().stream().map(item -> new Item(item.getId(), item.getRecetaId(), item.getNombreReceta(),
                        item.getCantidad(), item.getCantidadDevuelta(), item.getPrecioUnitario(), item.subtotal())).toList(),
                comanda.getDevoluciones().stream().map(d -> new Devolucion(d.getItemId(), d.getNombreReceta(),
                        d.getCantidad(), d.getImporte(), d.getMotivo(), d.getRegistradaEn())).toList(),
                comanda.getRegistradaEn(), comanda.getPreparadaEn(), comanda.getDespachadaEn(),
                comanda.getAnuladaEn(), comanda.getEstadoAlAnular(), comanda.getMotivoAnulacion());
    }

    public record Item(UUID id, UUID recetaId, String nombre, int cantidad, int cantidadDevuelta,
                       BigDecimal precioUnitario, BigDecimal subtotal) {
    }

    public record Devolucion(UUID itemId, String trago, int cantidad, BigDecimal importe, String motivo, Instant fecha) {
    }
}
