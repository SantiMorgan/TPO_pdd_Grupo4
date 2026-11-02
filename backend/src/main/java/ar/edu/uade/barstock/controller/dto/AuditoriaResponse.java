package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuditoriaResponse(UUID id, int numero, String responsable, Instant fecha, BigDecimal mermaTotal,
                                long diferencias, List<Detalle> detalles) {
    public static AuditoriaResponse desde(Auditoria auditoria) {
        return new AuditoriaResponse(auditoria.getId(), auditoria.getNumero(), auditoria.getResponsable(),
                auditoria.getFecha(), auditoria.getMermaTotal(), auditoria.diferenciasDetectadas(),
                auditoria.getDetalles().stream().map(d -> new Detalle(d.getInsumoId(), d.getNombreInsumo(),
                        d.getUnidadMedida(), d.getStockTeorico(), d.getStockFisico(), d.getDiferencia(),
                        d.getCostoMerma())).toList());
    }

    public record Detalle(UUID insumoId, String insumo, UnidadMedida unidadMedida, BigDecimal stockTeorico,
                          BigDecimal stockFisico, BigDecimal diferencia, BigDecimal costoMerma) {
    }
}
