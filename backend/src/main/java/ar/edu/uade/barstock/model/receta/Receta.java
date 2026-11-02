package ar.edu.uade.barstock.model.receta;

import ar.edu.uade.barstock.model.Validacion;
import ar.edu.uade.barstock.model.inventario.Consumo;
import ar.edu.uade.barstock.model.inventario.Insumo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Trago del menú: define cuánto de cada insumo se descuenta por unidad vendida (stock fraccionado). */
@Entity
@Table(name = "receta", schema = "barstock")
public class Receta {
    @Id
    private UUID id;

    @Column(name = "establecimiento_id", nullable = false)
    private UUID establecimientoId;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false, length = 16)
    private String icono;

    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(nullable = false)
    private boolean activa;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("posicion")
    private List<IngredienteReceta> ingredientes = new ArrayList<>();

    @Version
    private long version;

    protected Receta() {
        // Constructor requerido por JPA.
    }

    public Receta(UUID establecimientoId, String nombre, String descripcion, String icono, BigDecimal precioBase,
                  Map<Insumo, Integer> ingredientes) {
        if (establecimientoId == null) {
            throw new IllegalArgumentException("El establecimiento es obligatorio");
        }
        this.id = UUID.randomUUID();
        this.establecimientoId = establecimientoId;
        this.activa = true;
        editar(nombre, descripcion, icono, precioBase, ingredientes);
    }

    public void editar(String nombre, String descripcion, String icono, BigDecimal precioBase,
                       Map<Insumo, Integer> nuevosIngredientes) {
        String nombreValidado = Validacion.texto(nombre, 80, "nombre");
        String descripcionValidada = Validacion.textoOpcional(descripcion, 200, "descripcion");
        String iconoValidado = Validacion.textoOpcional(icono, 16, "icono");
        BigDecimal precioValidado = precioValido(precioBase);
        reemplazarIngredientes(nuevosIngredientes);
        this.nombre = nombreValidado;
        this.descripcion = descripcionValidada;
        this.icono = iconoValidado.isEmpty() ? "🍹" : iconoValidado;
        this.precioBase = precioValidado;
    }

    /** Conserva las filas de los insumos que siguen en la receta para no violar la clave única. */
    private void reemplazarIngredientes(Map<Insumo, Integer> nuevos) {
        if (nuevos == null || nuevos.isEmpty()) {
            throw new IllegalArgumentException("La receta necesita al menos un ingrediente");
        }
        Map<UUID, IngredienteReceta> actuales = new LinkedHashMap<>();
        ingredientes.forEach(ingrediente -> actuales.put(ingrediente.getInsumo().getId(), ingrediente));
        List<IngredienteReceta> resultado = new ArrayList<>();
        int posicion = 0;
        for (Map.Entry<Insumo, Integer> entrada : nuevos.entrySet()) {
            Insumo insumo = entrada.getKey();
            if (!insumo.perteneceA(establecimientoId)) {
                throw new IllegalArgumentException("El insumo " + insumo.getNombre() + " es de otro establecimiento");
            }
            int cantidad = entrada.getValue() == null ? 0 : entrada.getValue();
            IngredienteReceta existente = actuales.remove(insumo.getId());
            if (existente != null) {
                existente.actualizar(cantidad, posicion);
                resultado.add(existente);
            } else {
                resultado.add(new IngredienteReceta(this, insumo, cantidad, posicion));
            }
            posicion++;
        }
        // Los que no volvieron a aparecer se eliminan por orphanRemoval.
        ingredientes.clear();
        ingredientes.addAll(resultado);
    }

    /** Experto en información: la receta conoce sus ingredientes, entonces calcula su costo teórico. */
    public BigDecimal calcularCosto() {
        return ingredientes.stream().map(IngredienteReceta::costo)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    public List<Consumo> consumos(int unidades) {
        if (unidades < 1) {
            throw new IllegalArgumentException("La cantidad de tragos debe ser mayor a cero");
        }
        return ingredientes.stream().map(ingrediente -> ingrediente.consumoPara(unidades)).toList();
    }

    /** Si hoy alcanza el stock para preparar al menos una unidad. No reserva nada. */
    public boolean disponible() {
        return activa && ingredientes.stream().allMatch(IngredienteReceta::alcanzaParaUnaUnidad);
    }

    public void quitarDelMenu() {
        activa = false;
    }

    public void volverAlMenu() {
        activa = true;
    }

    public boolean perteneceA(UUID establecimiento) {
        return establecimientoId.equals(establecimiento);
    }

    private static BigDecimal precioValido(BigDecimal precio) {
        if (precio == null || precio.signum() <= 0) {
            throw new IllegalArgumentException("precioBase debe ser mayor a cero");
        }
        try {
            BigDecimal normalizado = precio.setScale(2, RoundingMode.UNNECESSARY);
            if (normalizado.precision() > 12) {
                throw new IllegalArgumentException("precioBase supera la precisión máxima");
            }
            return normalizado;
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("precioBase admite hasta 2 decimales", ex);
        }
    }

    public UUID getId() { return id; }
    public UUID getEstablecimientoId() { return establecimientoId; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getIcono() { return icono; }
    public BigDecimal getPrecioBase() { return precioBase; }
    public boolean isActiva() { return activa; }
    public List<IngredienteReceta> getIngredientes() { return List.copyOf(ingredientes); }
}
