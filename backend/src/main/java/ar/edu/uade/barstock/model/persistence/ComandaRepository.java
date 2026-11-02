package ar.edu.uade.barstock.model.persistence;

import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.state.EstadoComanda;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComandaRepository extends JpaRepository<Comanda, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Comanda c where c.id = :id")
    Optional<Comanda> buscarParaActualizar(@Param("id") UUID id);

    @Query("select c from Comanda c where c.establecimientoId = :establecimiento and c.estado in :estados order by c.numero")
    List<Comanda> buscarPorEstados(@Param("establecimiento") UUID establecimientoId,
                                   @Param("estados") Collection<EstadoComanda> estados);

    List<Comanda> findByEstablecimientoIdOrderByNumeroDesc(UUID establecimientoId, Pageable pagina);

    @Query("select count(c) from Comanda c where c.establecimientoId = :establecimiento and c.estado in :estados")
    long contarPorEstados(@Param("establecimiento") UUID establecimientoId,
                          @Param("estados") Collection<EstadoComanda> estados);

    List<Comanda> findByEstablecimientoIdAndDespachadaEnIsNotNullOrderByDespachadaEnAsc(UUID establecimientoId);
}
