package ar.edu.uade.barstock.model.establecimiento;

import ar.edu.uade.barstock.model.Validacion;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Bar con inventario, menú, comandas, política de precios y alertas propias. */
@Entity
@Table(name = "establecimiento", schema = "barstock")
public class Establecimiento {
    @Id
    private UUID id;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String zona;

    @Column(nullable = false, length = 150)
    private String descripcion;

    @Column(name = "proximo_numero_comanda", nullable = false)
    private int proximoNumeroComanda;

    @Column(name = "proximo_numero_auditoria", nullable = false)
    private int proximoNumeroAuditoria;

    @Embedded
    private PoliticaPrecio politicaPrecio;

    @Embedded
    private ConfiguracionAlertas alertas;

    @Version
    private long version;

    protected Establecimiento() {
        // Constructor requerido por JPA.
    }

    public Establecimiento(String codigo, String nombre, String zona, String descripcion) {
        this.id = UUID.randomUUID();
        this.codigo = normalizarCodigo(codigo);
        editar(nombre, zona, descripcion);
        this.proximoNumeroComanda = 1;
        this.proximoNumeroAuditoria = 1;
        this.politicaPrecio = PoliticaPrecio.porDefecto();
        this.alertas = ConfiguracionAlertas.porDefecto();
    }

    public static String normalizarCodigo(String codigo) {
        return Validacion.texto(codigo, 30, "codigo").toUpperCase(Locale.ROOT);
    }

    public void editar(String nombre, String zona, String descripcion) {
        String nombreValidado = Validacion.texto(nombre, 80, "nombre");
        String zonaValidada = Validacion.texto(zona, 80, "zona");
        String descripcionValidada = Validacion.textoOpcional(descripcion, 150, "descripcion");
        this.nombre = nombreValidado;
        this.zona = zonaValidada;
        this.descripcion = descripcionValidada;
    }

    /** Requiere la fila bloqueada para que dos comandas simultáneas no reciban el mismo número. */
    public int asignarNumeroComanda() {
        return proximoNumeroComanda++;
    }

    public int asignarNumeroAuditoria() {
        return proximoNumeroAuditoria++;
    }

    public int verProximoNumeroAuditoria() {
        return proximoNumeroAuditoria;
    }

    public void cambiarPoliticaPrecio(PoliticaPrecio politica) {
        this.politicaPrecio = Objects.requireNonNull(politica, "La política de precios es obligatoria");
    }

    public void cambiarAlertas(ConfiguracionAlertas configuracion) {
        this.alertas = Objects.requireNonNull(configuracion, "La configuración de alertas es obligatoria");
    }

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getZona() { return zona; }
    public String getDescripcion() { return descripcion; }
    public PoliticaPrecio getPoliticaPrecio() { return politicaPrecio; }
    public ConfiguracionAlertas getAlertas() { return alertas; }
}
