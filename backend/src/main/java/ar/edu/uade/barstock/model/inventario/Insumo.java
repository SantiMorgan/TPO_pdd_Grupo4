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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "insumo", schema = "barstock")
public class Insumo {
    private static final int CONTENIDO_MAXIMO = 999_999;

    @Id
    private UUID id;

    @Column(name = "establecimiento_id", nullable = false)
    private UUID establecimientoId;

    @Column(nullable = false, length = 50)
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

    /** Cantidad que trae cada envase comprado (por ejemplo, 750 ml por botella). */
    @Column(name = "contenido_envase", nullable = false)
    private int contenidoEnvase;

    @Column(nullable = false)
    private boolean activo;

    @Version
    private long version;

    protected Insumo() {
        // Constructor requerido por JPA.
    }

    public Insumo(UUID establecimientoId, String codigo, String nombre, UnidadMedida unidadMedida,
                  BigDecimal stockInicial, BigDecimal costoUnitario, BigDecimal puntoReorden, int contenidoEnvase) {
        if (establecimientoId == null) {
            throw new IllegalArgumentException("El establecimiento es obligatorio");
        }
        this.id = UUID.randomUUID();
        this.establecimientoId = establecimientoId;
        this.codigo = normalizarCodigo(codigo);
        if (unidadMedida == null) {
            throw new IllegalArgumentException("La unidad de medida es obligatoria");
        }
        this.unidadMedida = unidadMedida;
        this.stockActual = decimalValido(stockInicial, 0, "stockInicial");
        actualizarDatos(nombre, costoUnitario, puntoReorden, contenidoEnvase);
        this.activo = true;
    }

    public static String normalizarCodigo(String codigo) {
        return textoValido(codigo, 50, "codigo").toUpperCase(Locale.ROOT);
    }

    public void editar(String nombre, BigDecimal costoUnitario, BigDecimal puntoReorden) {
        editar(nombre, costoUnitario, puntoReorden, contenidoEnvase);
    }

    public void editar(String nombre, BigDecimal costoUnitario, BigDecimal puntoReorden, int contenidoEnvase) {
        if (!activo) {
            throw new InsumoInactivoException();
        }
        actualizarDatos(nombre, costoUnitario, puntoReorden, contenidoEnvase);
    }

    private void actualizarDatos(String nombre, BigDecimal costoUnitario, BigDecimal puntoReorden, int contenido) {
        String nombreValidado = textoValido(nombre, 150, "nombre");
        BigDecimal costoValidado = decimalValido(costoUnitario, 2, "costoUnitario");
        BigDecimal puntoValidado = decimalValido(puntoReorden, 0, "puntoReorden");
        if (contenido < 1 || contenido > CONTENIDO_MAXIMO) {
            throw new IllegalArgumentException("contenidoEnvase debe estar entre 1 y " + CONTENIDO_MAXIMO);
        }
        this.nombre = nombreValidado;
        this.costoUnitario = costoValidado;
        this.puntoReorden = puntoValidado;
        this.contenidoEnvase = contenido;
    }

    public void darDeBaja() {
        activo = false;
    }

    public void darDeAlta() {
        activo = true;
    }

    public boolean alcanza(BigDecimal cantidad) {
        return stockActual.compareTo(cantidad) >= 0;
    }

    /** El Inventario valida todos los insumos antes de descontar; esta guarda protege el invariante. */
    public void descontar(BigDecimal cantidad) {
        BigDecimal validada = cantidadPositiva(cantidad);
        if (!alcanza(validada)) {
            throw new StockInsuficienteException(List.of(Faltante.de(this, validada)));
        }
        stockActual = stockActual.subtract(validada);
    }

    public void reponer(BigDecimal cantidad) {
        stockActual = decimalValido(stockActual.add(cantidadPositiva(cantidad)), 0, "stockActual");
    }

    /** Lleva el stock al valor contado y devuelve la diferencia (físico − teórico). */
    public BigDecimal ajustarA(BigDecimal stockFisico) {
        BigDecimal fisico = decimalValido(stockFisico, 0, "stockFisico");
        BigDecimal diferencia = fisico.subtract(stockActual);
        stockActual = fisico;
        return diferencia;
    }

    public BigDecimal costoDe(BigDecimal cantidad) {
        return cantidad.multiply(costoUnitario).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal costoEnvase() {
        return costoDe(BigDecimal.valueOf(contenidoEnvase));
    }

    public BigDecimal valorizacion() {
        return costoDe(stockActual);
    }

    public boolean perteneceA(UUID establecimiento) {
        return establecimientoId.equals(establecimiento);
    }

    private static BigDecimal cantidadPositiva(BigDecimal cantidad) {
        BigDecimal validada = decimalValido(cantidad, 0, "cantidad");
        if (validada.signum() == 0) {
            throw new IllegalArgumentException("cantidad debe ser mayor a cero");
        }
        return validada;
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
    public UUID getEstablecimientoId() { return establecimientoId; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public BigDecimal getStockActual() { return stockActual; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public BigDecimal getPuntoReorden() { return puntoReorden; }
    public int getContenidoEnvase() { return contenidoEnvase; }
    public boolean isActivo() { return activo; }
    public boolean necesitaReposicion() { return stockActual.compareTo(puntoReorden) <= 0; }
}
