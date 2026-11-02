package ar.edu.uade.barstock.model.comanda;

import ar.edu.uade.barstock.model.receta.Receta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

/** Línea de la comanda. Guarda el precio y el costo del momento en que se registró. */
@Entity
@Table(name = "item_comanda", schema = "barstock")
public class ItemComanda {
    static final int CANTIDAD_MAXIMA = 99;

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comanda_id", nullable = false)
    private Comanda comanda;

    @Column(name = "receta_id", nullable = false)
    private UUID recetaId;

    @Column(name = "nombre_receta", nullable = false, length = 80)
    private String nombreReceta;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(nullable = false)
    private int posicion;

    protected ItemComanda() {
        // Constructor requerido por JPA.
    }

    ItemComanda(Comanda comanda, Receta receta, int cantidad, BigDecimal precioUnitario, int posicion) {
        if (cantidad < 1 || cantidad > CANTIDAD_MAXIMA) {
            throw new IllegalArgumentException("La cantidad de " + receta.getNombre() + " debe estar entre 1 y " + CANTIDAD_MAXIMA);
        }
        this.id = UUID.randomUUID();
        this.comanda = comanda;
        this.recetaId = receta.getId();
        this.nombreReceta = receta.getNombre();
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoUnitario = receta.calcularCosto();
        this.posicion = posicion;
    }

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public BigDecimal costo() {
        return costoUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public UUID getId() { return id; }
    public UUID getRecetaId() { return recetaId; }
    public String getNombreReceta() { return nombreReceta; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
}
