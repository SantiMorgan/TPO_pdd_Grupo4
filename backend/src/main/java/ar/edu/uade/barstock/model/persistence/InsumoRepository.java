package ar.edu.uade.barstock.model.persistence;

import ar.edu.uade.barstock.model.inventario.Insumo;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InsumoRepository extends JpaRepository<Insumo, UUID> {
    boolean existsByEstablecimientoIdAndCodigo(UUID establecimientoId, String codigo);

    List<Insumo> findByEstablecimientoIdAndActivoOrderByNombreAscIdAsc(UUID establecimientoId, boolean activo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Insumo i where i.id = :id")
    Optional<Insumo> buscarParaActualizar(@Param("id") UUID id);
}
