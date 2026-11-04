package ar.edu.uade.barstock.model.receta;

import ar.edu.uade.barstock.model.inventario.Consumo;
import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.inventario.InsumoInactivoException;
import ar.edu.uade.barstock.model.inventario.InsumoNoEncontradoException;
import ar.edu.uade.barstock.model.persistence.InsumoRepository;
import ar.edu.uade.barstock.model.persistence.RecetaRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RecetaService {
    private final RecetaRepository recetas;
    private final InsumoRepository insumos;

    public RecetaService(RecetaRepository recetas, InsumoRepository insumos) {
        this.recetas = recetas;
        this.insumos = insumos;
    }

    public List<Receta> listar(UUID establecimientoId, boolean incluirFueraDelMenu) {
        return incluirFueraDelMenu
                ? recetas.findByEstablecimientoIdOrderByNombreAsc(establecimientoId)
                : recetas.findByEstablecimientoIdAndActivaTrueOrderByNombreAsc(establecimientoId);
    }

    public Receta consultar(UUID establecimientoId, UUID id) {
        return recetas.findById(id)
                .filter(receta -> receta.perteneceA(establecimientoId))
                .orElseThrow(() -> new RecetaNoEncontradaException(id));
    }

    @Transactional
    public Receta crear(UUID establecimientoId, String nombre, String descripcion, String icono,
                        BigDecimal precioBase, Map<UUID, Integer> ingredientes) {
        if (nombre != null && recetas.existsByEstablecimientoIdAndNombreIgnoreCase(establecimientoId, nombre.strip())) {
            throw new NombreRecetaDuplicadoException(nombre.strip());
        }
        Receta receta = new Receta(establecimientoId, nombre, descripcion, icono, precioBase,
                resolverInsumos(establecimientoId, ingredientes));
        return recetas.saveAndFlush(receta);
    }

    @Transactional
    public Receta editar(UUID establecimientoId, UUID id, String nombre, String descripcion, String icono,
                         BigDecimal precioBase, Map<UUID, Integer> ingredientes) {
        Receta receta = consultar(establecimientoId, id);
        if (nombre != null && recetas.existsByEstablecimientoIdAndNombreIgnoreCaseAndIdNot(establecimientoId, nombre.strip(), id)) {
            throw new NombreRecetaDuplicadoException(nombre.strip());
        }
        receta.editar(nombre, descripcion, icono, precioBase, resolverInsumos(establecimientoId, ingredientes));
        return recetas.saveAndFlush(receta);
    }

    @Transactional
    public Receta quitarDelMenu(UUID establecimientoId, UUID id) {
        Receta receta = consultar(establecimientoId, id);
        receta.quitarDelMenu();
        return receta;
    }

    @Transactional
    public Receta volverAlMenu(UUID establecimientoId, UUID id) {
        Receta receta = consultar(establecimientoId, id);
        receta.volverAlMenu();
        return receta;
    }

    /** Agrupa el pedido por receta y exige que cada una esté en el menú del establecimiento. */
    public Map<Receta, Integer> armarPedido(UUID establecimientoId, List<CantidadReceta> pedido) {
        if (pedido == null || pedido.isEmpty()) {
            throw new IllegalArgumentException("Agregá al menos un trago");
        }
        Map<UUID, Integer> cantidades = new LinkedHashMap<>();
        pedido.forEach(item -> cantidades.merge(item.recetaId(), item.cantidad(), Integer::sum));
        Map<Receta, Integer> resultado = new LinkedHashMap<>();
        cantidades.forEach((recetaId, cantidad) -> {
            Receta receta = consultar(establecimientoId, recetaId);
            if (!receta.isActiva()) {
                throw new RecetaFueraDelMenuException(receta.getNombre());
            }
            resultado.put(receta, cantidad);
        });
        return resultado;
    }

    /** Traduce lo pedido a consumos de insumos. Usa la receta actual aunque luego haya salido del menú. */
    public List<Consumo> consumosDe(UUID establecimientoId, List<CantidadReceta> pedido) {
        List<Consumo> consumos = new ArrayList<>();
        for (CantidadReceta item : pedido) {
            consumos.addAll(consultar(establecimientoId, item.recetaId()).consumos(item.cantidad()));
        }
        return consumos;
    }

    private Map<Insumo, Integer> resolverInsumos(UUID establecimientoId, Map<UUID, Integer> ingredientes) {
        if (ingredientes == null || ingredientes.isEmpty()) {
            throw new IllegalArgumentException("La receta necesita al menos un ingrediente");
        }
        Map<Insumo, Integer> resultado = new LinkedHashMap<>();
        ingredientes.forEach((insumoId, cantidad) -> {
            Insumo insumo = insumos.findById(insumoId)
                    .filter(i -> i.perteneceA(establecimientoId))
                    .orElseThrow(() -> new InsumoNoEncontradoException(insumoId));
            if (!insumo.isActivo()) {
                throw new InsumoInactivoException(insumo.getNombre());
            }
            resultado.put(insumo, cantidad);
        });
        return resultado;
    }
}
