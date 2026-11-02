package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.persistence.InsumoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Alta, consulta, edición y baja lógica de insumos. Los cambios de stock pasan por Inventario. */
@Service
@Transactional(readOnly = true)
public class InventarioService {
    private final InsumoRepository repositorio;

    public InventarioService(InsumoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public Insumo crear(UUID establecimientoId, String codigo, String nombre, UnidadMedida unidadMedida,
                        BigDecimal stockInicial, BigDecimal costoUnitario, BigDecimal puntoReorden,
                        int contenidoEnvase) {
        Insumo insumo = new Insumo(establecimientoId, codigo, nombre, unidadMedida, stockInicial,
                costoUnitario, puntoReorden, contenidoEnvase);
        if (repositorio.existsByEstablecimientoIdAndCodigo(establecimientoId, insumo.getCodigo())) {
            throw new CodigoInsumoDuplicadoException();
        }
        // La restricción UNIQUE (establecimiento, código) también protege las creaciones concurrentes.
        return repositorio.saveAndFlush(insumo);
    }

    public List<Insumo> listar(UUID establecimientoId, boolean activo) {
        return repositorio.findByEstablecimientoIdAndActivoOrderByNombreAscIdAsc(establecimientoId, activo);
    }

    public Insumo consultar(UUID establecimientoId, UUID id) {
        return repositorio.findById(id)
                .filter(insumo -> insumo.perteneceA(establecimientoId))
                .orElseThrow(() -> new InsumoNoEncontradoException(id));
    }

    @Transactional
    public Insumo editar(UUID establecimientoId, UUID id, String nombre, BigDecimal costoUnitario,
                         BigDecimal puntoReorden, Integer contenidoEnvase) {
        Insumo insumo = buscarParaActualizar(establecimientoId, id);
        insumo.editar(nombre, costoUnitario, puntoReorden,
                contenidoEnvase == null ? insumo.getContenidoEnvase() : contenidoEnvase);
        return insumo;
    }

    @Transactional
    public void darDeBaja(UUID establecimientoId, UUID id) {
        buscarParaActualizar(establecimientoId, id).darDeBaja();
    }

    @Transactional
    public Insumo darDeAlta(UUID establecimientoId, UUID id) {
        Insumo insumo = buscarParaActualizar(establecimientoId, id);
        insumo.darDeAlta();
        return insumo;
    }

    private Insumo buscarParaActualizar(UUID establecimientoId, UUID id) {
        return repositorio.buscarParaActualizar(id)
                .filter(insumo -> insumo.perteneceA(establecimientoId))
                .orElseThrow(() -> new InsumoNoEncontradoException(id));
    }
}
