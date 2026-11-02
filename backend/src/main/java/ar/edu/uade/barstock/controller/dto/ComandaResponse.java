package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.OrigenComanda;
import ar.edu.uade.barstock.model.comanda.state.AccionComanda;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** {@code acciones} sale del estado actual (State): la interfaz muestra solo los botones válidos. */
public record ComandaResponse(UUID id, int numero, String estado, String estadoNombre, Set<AccionComanda> acciones,
                              OrigenComanda origen, String cliente, String estrategiaPrecio, BigDecimal total,
                              BigDecimal costoTotal, List<Item> items, Instant registradaEn, Instant preparadaEn,
                              Instant despachadaEn, Instant anuladaEn, String estadoAlAnular, String motivoAnulacion) {
    public static ComandaResponse desde(Comanda comanda) {
        return new ComandaResponse(comanda.getId(), comanda.getNumero(), comanda.getEstado().codigo(),
                comanda.getEstado().nombre(), comanda.accionesPermitidas(), comanda.getOrigen(), comanda.getCliente(),
                comanda.getEstrategiaPrecio(), comanda.getTotal(), comanda.getCostoTotal(),
                comanda.getItems().stream().map(item -> new Item(item.getRecetaId(), item.getNombreReceta(),
                        item.getCantidad(), item.getPrecioUnitario(), item.subtotal())).toList(),
                comanda.getRegistradaEn(), comanda.getPreparadaEn(), comanda.getDespachadaEn(),
                comanda.getAnuladaEn(), comanda.getEstadoAlAnular(), comanda.getMotivoAnulacion());
    }

    public record Item(UUID recetaId, String nombre, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
    }
}
