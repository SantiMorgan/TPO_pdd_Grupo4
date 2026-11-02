package ar.edu.uade.barstock.model.comanda;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Tragos devueltos de una comanda despachada. Se reintegra el importe, no los insumos. */
@Entity
@Table(name = "devolucion_item", schema = "barstock")
public class DevolucionItem {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comanda_id", nullable = false)
    private Comanda comanda;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "nombre_receta", nullable = false, length = 80)
    private String nombreReceta;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(nullable = false, length = 220)
    private String motivo;

    @Column(name = "registrada_en", nullable = false)
    private Instant registradaEn;

    protected DevolucionItem() {
        // Constructor requerido por JPA.
    }

    DevolucionItem(Comanda comanda, ItemComanda item, int cantidad, String motivo, Instant registradaEn) {
        this.id = UUID.randomUUID();
        this.comanda = comanda;
        this.itemId = item.getId();
        this.nombreReceta = item.getNombreReceta();
        this.cantidad = cantidad;
        this.importe = item.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));
        this.motivo = motivo;
        this.registradaEn = registradaEn;
    }

    public UUID getId() { return id; }
    public UUID getItemId() { return itemId; }
    public String getNombreReceta() { return nombreReceta; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getImporte() { return importe; }
    public String getMotivo() { return motivo; }
    public Instant getRegistradaEn() { return registradaEn; }
}
