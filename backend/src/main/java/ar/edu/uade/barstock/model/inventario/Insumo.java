package ar.edu.uade.barstock.model.inventario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "insumo", schema = "barstock")
public class Insumo {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false, length = 10)
    private UnidadMedida unidadMedida;

    @Column(name = "stock_actual", nullable = false, precision = 12, scale = 0)
    private BigDecimal stockActual;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "punto_reorden", nullable = false, precision = 12, scale = 0)
    private BigDecimal puntoReorden;

    @Column(nullable = false)
    private boolean activo;

    @Version
    private long version;

    protected Insumo() {
        // Constructor requerido por JPA.
    }

    public Insumo(String codigo, String nombre, UnidadMedida unidadMedida,
                  BigDecimal stockInicial, BigDecimal costoUnitario, BigDecimal puntoReorden) {
        this.id = UUID.randomUUID();
        this.codigo = normalizarCodigo(codigo);
        if (unidadMedida == null) {
            throw new IllegalArgumentException("La unidad de medida es obligatoria");
        }
        this.unidadMedida = unidadMedida;
        this.stockActual = decimalValido(stockInicial, 0, "stockInicial");
        actualizarDatos(nombre, costoUnitario, puntoReorden);
        this.activo = true;
    }

    public static String normalizarCodigo(String codigo) {
        return textoValido(codigo, 50, "codigo").toUpperCase(Locale.ROOT);
    }

    public void editar(String nombre, BigDecimal costoUnitario, BigDecimal puntoReorden) {
        if (!activo) {
            throw new InsumoInactivoException();
        }
        actualizarDatos(nombre, costoUnitario, puntoReorden);
    }

    private void actualizarDatos(String nombre, BigDecimal costoUnitario, BigDecimal puntoReorden) {
        String nombreValidado = textoValido(nombre, 150, "nombre");
        BigDecimal costoValidado = decimalValido(costoUnitario, 2, "costoUnitario");
        BigDecimal puntoValidado = decimalValido(puntoReorden, 0, "puntoReorden");
        this.nombre = nombreValidado;
        this.costoUnitario = costoValidado;
        this.puntoReorden = puntoValidado;
    }

    public void darDeBaja() {
        activo = false;
    }

    public void darDeAlta() {
        activo = true;
    }

    private static String textoValido(String texto, int longitudMaxima, String campo) {
        if (texto == null || texto.isBlank() || texto.strip().length() > longitudMaxima) {
            throw new IllegalArgumentException(campo + " es obligatorio y admite hasta " + longitudMaxima + " caracteres");
        }
        return texto.strip();
    }

    private static BigDecimal decimalValido(BigDecimal valor, int escala, String campo) {
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException(campo + " debe ser mayor o igual a cero");
        }
        if (escala == 0 && valor.scale() > 0) {
            throw new IllegalArgumentException(campo + " debe ser un número entero sin decimales");
        }
        BigDecimal normalizado;
        try {
            normalizado = valor.setScale(escala, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(campo + " admite hasta " + escala + " decimales", ex);
        }
        if (normalizado.precision() > 12) {
            throw new IllegalArgumentException(campo + " supera la precisión máxima de 12 dígitos");
        }
        return normalizado;
    }

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public BigDecimal getStockActual() { return stockActual; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public BigDecimal getPuntoReorden() { return puntoReorden; }
    public boolean isActivo() { return activo; }
    public boolean necesitaReposicion() { return stockActual.compareTo(puntoReorden) <= 0; }
}
