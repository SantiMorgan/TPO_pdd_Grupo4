package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.auditoria.DetalleAuditoria;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Agrega las mermas auditadas por insumo, de la más costosa a la menor, con la cantidad perdida
 * en la unidad base y en envases (botellas) y los comentarios que dejaron al contar.
 */
public class ReporteConMermas extends ReporteDecorator {
    public ReporteConMermas(Reporte reporte, DatosReporte datos) {
        super(reporte, datos);
    }

    @Override
    protected SeccionReporte seccion() {
        Map<List<UUID>, MermaInsumo> porInsumo = new LinkedHashMap<>();
        for (Auditoria auditoria : datos.auditorias()) {
            for (DetalleAuditoria detalle : auditoria.getDetalles()) {
                if (detalle.getDiferencia().signum() < 0) {
                    List<String> comentarios = detalle.getComentario() == null ? List.of() : List.of(detalle.getComentario());
                    MermaInsumo nueva = new MermaInsumo(datos.nombreDe(auditoria.getEstablecimientoId()),
                            detalle.getNombreInsumo(), detalle.getUnidadMedida(), detalle.getDiferencia().negate(),
                            detalle.envasesPerdidos(), detalle.getCostoMerma(), comentarios);
                    porInsumo.merge(List.of(auditoria.getEstablecimientoId(), detalle.getInsumoId()), nueva, MermaInsumo::sumar);
                }
            }
        }
        List<MermaInsumo> mermas = porInsumo.values().stream()
                .sorted(Comparator.comparing(MermaInsumo::costo).reversed()).toList();
        return new SeccionReporte("MERMAS", "Mermas por insumo", mermas);
    }

    public record MermaInsumo(String establecimiento, String insumo, UnidadMedida unidad, BigDecimal cantidadPerdida,
                              BigDecimal envasesPerdidos, BigDecimal costo, List<String> comentarios) {
        MermaInsumo sumar(MermaInsumo otra) {
            List<String> todos = new ArrayList<>(comentarios);
            otra.comentarios.stream().filter(c -> !todos.contains(c)).forEach(todos::add);
            return new MermaInsumo(establecimiento, insumo, unidad, cantidadPerdida.add(otra.cantidadPerdida),
                    envasesPerdidos.add(otra.envasesPerdidos), costo.add(otra.costo), List.copyOf(todos));
        }
    }
}
