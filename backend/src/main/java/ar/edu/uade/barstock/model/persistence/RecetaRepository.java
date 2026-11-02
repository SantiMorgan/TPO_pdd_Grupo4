package ar.edu.uade.barstock.model.persistence;

import ar.edu.uade.barstock.model.receta.Receta;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecetaRepository extends JpaRepository<Receta, UUID> {
    List<Receta> findByEstablecimientoIdOrderByNombreAsc(UUID establecimientoId);

    List<Receta> findByEstablecimientoIdAndActivaTrueOrderByNombreAsc(UUID establecimientoId);

    boolean existsByEstablecimientoIdAndNombreIgnoreCase(UUID establecimientoId, String nombre);

    boolean existsByEstablecimientoIdAndNombreIgnoreCaseAndIdNot(UUID establecimientoId, String nombre, UUID id);
}
