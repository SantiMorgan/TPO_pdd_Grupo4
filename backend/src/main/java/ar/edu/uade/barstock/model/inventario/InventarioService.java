package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.persistence.InsumoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventarioService {
    private final InsumoRepository repositorio;

    public InventarioService(InsumoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public Insumo crear(String codigo, String nombre, UnidadMedida unidadMedida,
                        BigDecimal stockInicial, BigDecimal costoUnitario, BigDecimal puntoReorden) {
        Insumo insumo = new Insumo(codigo, nombre, unidadMedida, stockInicial, costoUnitario, puntoReorden);
        if (repositorio.existsByCodigo(insumo.getCodigo())) {
            throw new CodigoInsumoDuplicadoException();
        }
        // La restricción UNIQUE también protege las creaciones concurrentes.
        return repositorio.saveAndFlush(insumo);
    }

    public List<Insumo> listar(boolean activo) {
        return repositorio.findByActivoOrderByNombreAscIdAsc(activo);
    }

    public Insumo consultar(UUID id) {
        return repositorio.findById(id).orElseThrow(() -> new InsumoNoEncontradoException(id));
    }

    @Transactional
    public Insumo editar(UUID id, String nombre, BigDecimal costoUnitario, BigDecimal puntoReorden) {
        Insumo insumo = buscarParaActualizar(id);
        insumo.editar(nombre, costoUnitario, puntoReorden);
        return insumo;
    }

    @Transactional
    public void darDeBaja(UUID id) {
        buscarParaActualizar(id).darDeBaja();
    }

    @Transactional
    public Insumo darDeAlta(UUID id) {
        Insumo insumo = buscarParaActualizar(id);
        insumo.darDeAlta();
        return insumo;
    }

    private Insumo buscarParaActualizar(UUID id) {
        return repositorio.buscarParaActualizar(id).orElseThrow(() -> new InsumoNoEncontradoException(id));
    }
}
