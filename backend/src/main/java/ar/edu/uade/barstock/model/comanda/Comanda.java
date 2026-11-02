package ar.edu.uade.barstock.model.comanda;

import ar.edu.uade.barstock.model.Validacion;
import ar.edu.uade.barstock.model.comanda.state.AccionComanda;
import ar.edu.uade.barstock.model.comanda.state.EstadoComanda;
import ar.edu.uade.barstock.model.comanda.state.EstadoComandaConverter;
import ar.edu.uade.barstock.model.comanda.state.EstadosComanda;
import ar.edu.uade.barstock.model.precio.EstrategiaPrecio;
import ar.edu.uade.barstock.model.receta.CantidadReceta;
import ar.edu.uade.barstock.model.receta.Receta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Contexto del patrón State: delega preparar, despachar y anular en su estado actual.
 * También es Creador (GRASP) de sus ítems y Experto en calcular su total.
 */
@Entity
@Table(name = "comanda", schema = "barstock")
public class Comanda {
    private static final int MINIMO_MOTIVO = 5;

    @Id
    private UUID id;

    @Column(name = "establecimiento_id", nullable = false)
    private UUID establecimientoId;

    @Column(nullable = false)
    private int numero;

    @Convert(converter = EstadoComandaConverter.class)
    @Column(nullable = false, length = 20)
    private EstadoComanda estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigenComanda origen;

    @Column(length = 60)
    private String cliente;

    @Column(name = "estrategia_precio", nullable = false, length = 60)
    private String estrategiaPrecio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "costo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Column(name = "registrada_en", nullable = false)
    private Instant registradaEn;

    @Column(name = "preparada_en")
    private Instant preparadaEn;

    @Column(name = "despachada_en")
    private Instant despachadaEn;

    @Column(name = "anulada_en")
    private Instant anuladaEn;

    @Column(name = "estado_al_anular", length = 20)
    private String estadoAlAnular;

    @Column(name = "motivo_anulacion", length = 220)
    private String motivoAnulacion;

    @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("posicion")
    private List<ItemComanda> items = new ArrayList<>();

    @Version
    private long version;

    protected Comanda() {
        // Constructor requerido por JPA.
    }

    /** El precio se fija con la estrategia vigente al registrar y no se recalcula (ADR-001 §4). */
    public static Comanda registrar(UUID establecimientoId, int numero, OrigenComanda origen, String cliente,
                                    EstrategiaPrecio estrategia, Map<Receta, Integer> pedido, Instant ahora) {
        if (pedido == null || pedido.isEmpty()) {
            throw new IllegalArgumentException("Agregá al menos un trago");
        }
        if (origen == null) {
            throw new IllegalArgumentException("El origen de la comanda es obligatorio");
        }
        Comanda comanda = new Comanda();
        comanda.id = UUID.randomUUID();
        comanda.establecimientoId = establecimientoId;
        comanda.numero = numero;
        comanda.estado = EstadosComanda.REGISTRADA;
        comanda.origen = origen;
        String nombre = Validacion.textoOpcional(cliente, 60, "cliente");
        comanda.cliente = nombre.isEmpty() ? null : nombre;
        comanda.estrategiaPrecio = estrategia.nombre();
        comanda.registradaEn = ahora;
        pedido.forEach((receta, cantidad) -> comanda.agregarItem(receta, cantidad, estrategia));
        comanda.total = comanda.calcularTotal();
        comanda.costoTotal = comanda.calcularCosto();
        return comanda;
    }

    private void agregarItem(Receta receta, int cantidad, EstrategiaPrecio estrategia) {
        items.add(new ItemComanda(this, receta, cantidad, estrategia.calcularPrecio(receta), items.size()));
    }

    public void preparar(Instant ahora) {
        estado.preparar(this, ahora);
    }

    public void despachar(Instant ahora) {
        estado.despachar(this, ahora);
    }

    public void anular(String motivo, Instant ahora) {
        if (motivo == null || motivo.strip().length() < MINIMO_MOTIVO) {
            throw new IllegalArgumentException("Indicá un motivo de al menos " + MINIMO_MOTIVO + " caracteres");
        }
        estado.anular(this, Validacion.texto(motivo, 220, "motivo"), ahora);
    }

    public BigDecimal calcularTotal() {
        return items.stream().map(ItemComanda::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularCosto() {
        return items.stream().map(ItemComanda::costo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<CantidadReceta> cantidadesPorReceta() {
        return items.stream().map(item -> new CantidadReceta(item.getRecetaId(), item.getCantidad())).toList();
    }

    public int unidades() {
        return items.stream().mapToInt(ItemComanda::getCantidad).sum();
    }

    public Set<AccionComanda> accionesPermitidas() {
        return estado.accionesPermitidas();
    }

    /** Ingreso que cuenta como venta: despachada y no devuelta. */
    public boolean cuentaComoVenta() {
        return estado == EstadosComanda.DESPACHADA;
    }

    /** Se despachó y después se anuló: consumió insumos pero su importe dejó de ser ingreso. */
    public boolean fueDevuelta() {
        return estado == EstadosComanda.ANULADA && despachadaEn != null;
    }

    // Operaciones que invocan los estados concretos.

    public void setEstado(EstadoComanda nuevo) {
        this.estado = nuevo;
    }

    public void registrarPreparacion(Instant ahora) {
        this.preparadaEn = ahora;
    }

    public void registrarDespacho(Instant ahora) {
        this.despachadaEn = ahora;
    }

    public void registrarAnulacion(EstadoComanda desde, String motivo, Instant ahora) {
        this.estadoAlAnular = desde.codigo();
        this.motivoAnulacion = motivo;
        this.anuladaEn = ahora;
    }

    public UUID getId() { return id; }
    public UUID getEstablecimientoId() { return establecimientoId; }
    public int getNumero() { return numero; }
    public EstadoComanda getEstado() { return estado; }
    public OrigenComanda getOrigen() { return origen; }
    public String getCliente() { return cliente; }
    public String getEstrategiaPrecio() { return estrategiaPrecio; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public Instant getRegistradaEn() { return registradaEn; }
    public Instant getPreparadaEn() { return preparadaEn; }
    public Instant getDespachadaEn() { return despachadaEn; }
    public Instant getAnuladaEn() { return anuladaEn; }
    public String getEstadoAlAnular() { return estadoAlAnular; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
    public List<ItemComanda> getItems() { return List.copyOf(items); }

    public boolean perteneceA(UUID establecimiento) {
        return establecimientoId.equals(establecimiento);
    }
}
