package ar.edu.uade.barstock.model.receta;

import ar.edu.uade.barstock.model.inventario.Consumo;
import ar.edu.uade.barstock.model.inventario.Insumo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ingrediente_receta", schema = "barstock")
public class IngredienteReceta {
    static final int CANTIDAD_MAXIMA = 99_999;

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private int posicion;

    protected IngredienteReceta() {
        // Constructor requerido por JPA.
    }

    IngredienteReceta(Receta receta, Insumo insumo, int cantidad, int posicion) {
        this.id = UUID.randomUUID();
        this.receta = receta;
        this.insumo = insumo;
        actualizar(cantidad, posicion);
    }

    void actualizar(int nuevaCantidad, int nuevaPosicion) {
        if (nuevaCantidad < 1 || nuevaCantidad > CANTIDAD_MAXIMA) {
            throw new IllegalArgumentException("La cantidad de " + insumo.getNombre() + " debe estar entre 1 y " + CANTIDAD_MAXIMA);
        }
        this.cantidad = nuevaCantidad;
        this.posicion = nuevaPosicion;
    }

    /** Sin redondeo intermedio: cantidad entera por costo con dos decimales. */
    public BigDecimal costo() {
        return insumo.getCostoUnitario().multiply(BigDecimal.valueOf(cantidad));
    }

    public Consumo consumoPara(int unidades) {
        return new Consumo(insumo.getId(), BigDecimal.valueOf((long) cantidad * unidades), insumo.getUnidadMedida());
    }

    public boolean alcanzaParaUnaUnidad() {
        return insumo.isActivo() && insumo.alcanza(BigDecimal.valueOf(cantidad));
    }

    public Insumo getInsumo() { return insumo; }
    public int getCantidad() { return cantidad; }
    public int getPosicion() { return posicion; }
}
