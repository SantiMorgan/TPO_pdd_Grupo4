package ar.edu.uade.barstock.model.auditoria;

import ar.edu.uade.barstock.model.Validacion;
import ar.edu.uade.barstock.model.inventario.AjusteStock;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Cierre de turno: compara el stock teórico con el recuento físico y valoriza la merma. */
@Entity
@Table(name = "auditoria", schema = "barstock")
public class Auditoria {
    @Id
    private UUID id;

    @Column(name = "establecimiento_id", nullable = false)
    private UUID establecimientoId;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false, length = 60)
    private String responsable;

    @Column(nullable = false)
    private Instant fecha;

    @Column(name = "merma_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal mermaTotal;

    @OneToMany(mappedBy = "auditoria", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("posicion")
    private List<DetalleAuditoria> detalles = new ArrayList<>();

    protected Auditoria() {
        // Constructor requerido por JPA.
    }

    public static Auditoria registrar(UUID establecimientoId, int numero, String responsable,
                                      List<AjusteStock> ajustes, Instant fecha) {
        if (ajustes == null || ajustes.isEmpty()) {
            throw new IllegalArgumentException("Ingresá por lo menos un recuento físico");
        }
        Auditoria auditoria = new Auditoria();
        auditoria.id = UUID.randomUUID();
        auditoria.establecimientoId = establecimientoId;
        auditoria.numero = numero;
        auditoria.responsable = Validacion.texto(responsable, 60, "responsable");
        auditoria.fecha = fecha;
        for (AjusteStock ajuste : ajustes) {
            auditoria.detalles.add(new DetalleAuditoria(auditoria, ajuste, auditoria.detalles.size()));
        }
        auditoria.mermaTotal = auditoria.detalles.stream().map(DetalleAuditoria::getCostoMerma)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return auditoria;
    }

    public long diferenciasDetectadas() {
        return detalles.stream().filter(detalle -> detalle.getDiferencia().signum() != 0).count();
    }

    public UUID getId() { return id; }
    public UUID getEstablecimientoId() { return establecimientoId; }
    public int getNumero() { return numero; }
    public String getResponsable() { return responsable; }
    public Instant getFecha() { return fecha; }
    public BigDecimal getMermaTotal() { return mermaTotal; }
    public List<DetalleAuditoria> getDetalles() { return List.copyOf(detalles); }
}
