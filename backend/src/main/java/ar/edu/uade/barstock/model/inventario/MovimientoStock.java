package ar.edu.uade.barstock.model.inventario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Registro inmutable de cada cambio de stock: compra, consumo por despacho o ajuste de auditoría. */
@Entity
@Table(name = "movimiento_stock", schema = "barstock")
public class MovimientoStock {
    @Id
    private UUID id;

    @Column(name = "establecimiento_id", nullable = false)
    private UUID establecimientoId;

    @Column(name = "insumo_id", nullable = false)
    private UUID insumoId;

    @Column(name = "nombre_insumo", nullable = false, length = 150)
    private String nombreInsumo;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false, length = 10)
    private UnidadMedida unidadMedida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    /** Positiva si ingresa stock, negativa si sale. */
    @Column(nullable = false)
    private BigDecimal cantidad;

    @Column(name = "stock_resultante", nullable = false)
    private BigDecimal stockResultante;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 160)
    private String detalle;

    @Column(nullable = false)
    private Instant fecha;

    protected MovimientoStock() {
        // Constructor requerido por JPA.
    }

    private MovimientoStock(Insumo insumo, TipoMovimiento tipo, BigDecimal cantidad, BigDecimal monto,
                            String detalle, Instant fecha) {
        this.id = UUID.randomUUID();
        this.establecimientoId = insumo.getEstablecimientoId();
        this.insumoId = insumo.getId();
        this.nombreInsumo = insumo.getNombre();
        this.unidadMedida = insumo.getUnidadMedida();
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockResultante = insumo.getStockActual();
        this.monto = monto;
        this.detalle = detalle.length() > 160 ? detalle.substring(0, 160) : detalle;
        this.fecha = fecha;
    }

    public static MovimientoStock compra(Insumo insumo, BigDecimal cantidad, String detalle, Instant fecha) {
        return new MovimientoStock(insumo, TipoMovimiento.COMPRA, cantidad, insumo.costoDe(cantidad), detalle, fecha);
    }

    public static MovimientoStock consumo(Insumo insumo, BigDecimal cantidad, String detalle, Instant fecha) {
        return new MovimientoStock(insumo, TipoMovimiento.CONSUMO, cantidad.negate(), BigDecimal.ZERO.setScale(2), detalle, fecha);
    }

    public static MovimientoStock ajuste(Insumo insumo, BigDecimal diferencia, String detalle, Instant fecha) {
        return new MovimientoStock(insumo, TipoMovimiento.AJUSTE, diferencia, BigDecimal.ZERO.setScale(2), detalle, fecha);
    }

    public UUID getId() { return id; }
    public UUID getEstablecimientoId() { return establecimientoId; }
    public UUID getInsumoId() { return insumoId; }
    public String getNombreInsumo() { return nombreInsumo; }
    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public TipoMovimiento getTipo() { return tipo; }
    public BigDecimal getCantidad() { return cantidad; }
    public BigDecimal getStockResultante() { return stockResultante; }
    public BigDecimal getMonto() { return monto; }
    public String getDetalle() { return detalle; }
    public Instant getFecha() { return fecha; }
}
