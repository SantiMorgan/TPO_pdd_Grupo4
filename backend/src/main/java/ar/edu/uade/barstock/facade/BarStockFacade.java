package ar.edu.uade.barstock.facade;

import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.inventario.InventarioService;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Punto de entrada a los casos de uso. Delega las reglas al modelo de dominio. */
@Service
public class BarStockFacade {
    private final InventarioService inventario;

    public BarStockFacade(InventarioService inventario) {
        this.inventario = inventario;
    }

    public String obtenerNombreAplicacion() {
        return "BarStock Solutions";
    }

    public Insumo crearInsumo(String codigo, String nombre, UnidadMedida unidadMedida,
                             BigDecimal stockInicial, BigDecimal costoUnitario, BigDecimal puntoReorden) {
        return inventario.crear(codigo, nombre, unidadMedida, stockInicial, costoUnitario, puntoReorden);
    }

    public List<Insumo> listarInsumos(boolean activo) {
        return inventario.listar(activo);
    }

    public Insumo consultarInsumo(UUID id) {
        return inventario.consultar(id);
    }

    public Insumo editarInsumo(UUID id, String nombre, BigDecimal costoUnitario, BigDecimal puntoReorden) {
        return inventario.editar(id, nombre, costoUnitario, puntoReorden);
    }

    public void darDeBajaInsumo(UUID id) {
        inventario.darDeBaja(id);
    }

    public Insumo darDeAltaInsumo(UUID id) {
        return inventario.darDeAlta(id);
    }
}
