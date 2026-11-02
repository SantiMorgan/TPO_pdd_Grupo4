package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.auditoria.DetalleAuditoria;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Agrega las mermas auditadas por insumo, de la más costosa a la menor. */
public class ReporteConMermas extends ReporteDecorator {
    public ReporteConMermas(Reporte reporte, DatosReporte datos) {
        super(reporte, datos);
    }

    @Override
    protected SeccionReporte seccion() {
        Map<String, MermaInsumo> porInsumo = new LinkedHashMap<>();
        for (Auditoria auditoria : datos.auditorias()) {
            for (DetalleAuditoria detalle : auditoria.getDetalles()) {
                if (detalle.getDiferencia().signum() < 0) {
                    MermaInsumo nueva = new MermaInsumo(detalle.getNombreInsumo(), detalle.getUnidadMedida(),
                            detalle.getDiferencia().negate(), detalle.getCostoMerma());
                    porInsumo.merge(detalle.getNombreInsumo(), nueva, MermaInsumo::sumar);
                }
            }
        }
        List<MermaInsumo> mermas = porInsumo.values().stream()
                .sorted(Comparator.comparing(MermaInsumo::costo).reversed()).toList();
        return new SeccionReporte("MERMAS", "Mermas por insumo", mermas);
    }

    public record MermaInsumo(String insumo, UnidadMedida unidad, BigDecimal cantidadPerdida, BigDecimal costo) {
        MermaInsumo sumar(MermaInsumo otra) {
            return new MermaInsumo(insumo, unidad, cantidadPerdida.add(otra.cantidadPerdida), costo.add(otra.costo));
        }
    }
}
