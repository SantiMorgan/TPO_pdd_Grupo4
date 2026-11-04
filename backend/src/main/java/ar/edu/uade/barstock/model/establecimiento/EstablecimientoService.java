package ar.edu.uade.barstock.model.establecimiento;

import ar.edu.uade.barstock.model.persistence.EstablecimientoRepository;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EstablecimientoService {
    private final EstablecimientoRepository repositorio;

    public EstablecimientoService(EstablecimientoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Establecimiento> listar() {
        return repositorio.findAllByOrderByNombreAsc();
    }

    /** Sin ids devuelve todos; si alguno no existe, falla. */
    public List<Establecimiento> listar(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return listar();
        }
        List<Establecimiento> encontrados = repositorio.findAllById(ids);
        for (UUID id : ids) {
            if (encontrados.stream().noneMatch(e -> e.getId().equals(id))) {
                throw new EstablecimientoNoEncontradoException(id);
            }
        }
        return encontrados.stream().sorted(Comparator.comparing(Establecimiento::getNombre)).toList();
    }

    public Establecimiento consultar(UUID id) {
        return repositorio.findById(id).orElseThrow(() -> new EstablecimientoNoEncontradoException(id));
    }

    public void verificarExistencia(UUID id) {
        if (!repositorio.existsById(id)) {
            throw new EstablecimientoNoEncontradoException(id);
        }
    }

    @Transactional
    public Establecimiento crear(String codigo, String nombre, String zona, String direccion, String descripcion,
                                 HorarioAtencion horario) {
        Establecimiento establecimiento = new Establecimiento(codigo, nombre, zona, direccion, descripcion);
        if (horario != null) {
            establecimiento.cambiarHorario(horario);
        }
        if (repositorio.existsByCodigo(establecimiento.getCodigo())) {
            throw new CodigoEstablecimientoDuplicadoException();
        }
        return repositorio.saveAndFlush(establecimiento);
    }

    @Transactional
    /** Sin {@code horario} se conserva el actual. */
    public Establecimiento editar(UUID id, String nombre, String zona, String direccion, String descripcion,
                                  HorarioAtencion horario) {
        Establecimiento establecimiento = bloquear(id);
        establecimiento.editar(nombre, zona, direccion, descripcion);
        if (horario != null) {
            establecimiento.cambiarHorario(horario);
        }
        return establecimiento;
    }

    @Transactional
    public Establecimiento cambiarPoliticaPrecio(UUID id, PoliticaPrecio politica) {
        Establecimiento establecimiento = bloquear(id);
        establecimiento.cambiarPoliticaPrecio(politica);
        return establecimiento;
    }

    @Transactional
    public Establecimiento cambiarAlertas(UUID id, ConfiguracionAlertas alertas) {
        Establecimiento establecimiento = bloquear(id);
        establecimiento.cambiarAlertas(alertas);
        return establecimiento;
    }

    /** Bloquea la fila hasta el fin de la transacción; se usa para numerar comandas y auditorías. */
    @Transactional
    public Establecimiento bloquear(UUID id) {
        return repositorio.buscarParaActualizar(id).orElseThrow(() -> new EstablecimientoNoEncontradoException(id));
    }
}
