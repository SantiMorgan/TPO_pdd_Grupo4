package ar.edu.uade.barstock.model.persistence;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditoriaRepository extends JpaRepository<Auditoria, UUID> {
    List<Auditoria> findByEstablecimientoIdOrderByNumeroDesc(UUID establecimientoId, Pageable pagina);

    @Query("select a from Auditoria a where a.establecimientoId in :establecimientos and a.fecha >= :desde"
            + " and a.fecha < :hasta order by a.fecha")
    List<Auditoria> buscarEntre(@Param("establecimientos") Collection<UUID> establecimientos,
                                @Param("desde") Instant desde, @Param("hasta") Instant hasta);
}
