package ar.edu.uade.barstock.model.persistence;

import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EstablecimientoRepository extends JpaRepository<Establecimiento, UUID> {
    boolean existsByCodigo(String codigo);

    List<Establecimiento> findAllByOrderByNombreAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Establecimiento e where e.id = :id")
    Optional<Establecimiento> buscarParaActualizar(@Param("id") UUID id);
}
