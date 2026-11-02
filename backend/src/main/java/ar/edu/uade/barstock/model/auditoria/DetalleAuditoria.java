package ar.edu.uade.barstock.model.auditoria;

import ar.edu.uade.barstock.model.inventario.AjusteStock;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "detalle_auditoria", schema = "barstock")
public class DetalleAuditoria {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditoria_id", nullable = false)
    private Auditoria auditoria;

    @Column(name = "insumo_id", nullable = false)
    private UUID insumoId;

    @Column(name = "nombre_insumo", nullable = false, length = 150)
    private String nombreInsumo;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false, length = 10)
    private UnidadMedida unidadMedida;

    @Column(name = "stock_teorico", nullable = false)
    private BigDecimal stockTeorico;

    @Column(name = "stock_fisico", nullable = false)
    private BigDecimal stockFisico;

    @Column(nullable = false)
    private BigDecimal diferencia;

    @Column(name = "costo_merma", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoMerma;

    @Column(nullable = false)
    private int posicion;

    protected DetalleAuditoria() {
        // Constructor requerido por JPA.
    }

    /** Diferencia = físico − teórico. Solo una diferencia negativa se valoriza como merma. */
    DetalleAuditoria(Auditoria auditoria, AjusteStock ajuste, int posicion) {
        this.id = UUID.randomUUID();
        this.auditoria = auditoria;
        this.insumoId = ajuste.insumoId();
        this.nombreInsumo = ajuste.nombre();
        this.unidadMedida = ajuste.unidad();
        this.stockTeorico = ajuste.stockTeorico();
        this.stockFisico = ajuste.stockFisico();
        this.diferencia = ajuste.diferencia();
        this.costoMerma = diferencia.signum() < 0
                ? diferencia.negate().multiply(ajuste.costoUnitario()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        this.posicion = posicion;
    }

    public UUID getInsumoId() { return insumoId; }
    public String getNombreInsumo() { return nombreInsumo; }
    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public BigDecimal getStockTeorico() { return stockTeorico; }
    public BigDecimal getStockFisico() { return stockFisico; }
    public BigDecimal getDiferencia() { return diferencia; }
    public BigDecimal getCostoMerma() { return costoMerma; }
    public int getPosicion() { return posicion; }
}
