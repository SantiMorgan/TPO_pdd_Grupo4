package ar.edu.uade.barstock.model.persistence;

import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import ar.edu.uade.barstock.model.inventario.TipoMovimiento;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, UUID> {
    List<MovimientoStock> findByEstablecimientoIdOrderByFechaDesc(UUID establecimientoId, Pageable pagina);

    @Query("select m from MovimientoStock m where m.establecimientoId in :establecimientos and m.tipo = :tipo"
            + " and m.fecha >= :desde and m.fecha < :hasta order by m.fecha")
    List<MovimientoStock> buscarEntre(@Param("establecimientos") Collection<UUID> establecimientos,
                                      @Param("tipo") TipoMovimiento tipo,
                                      @Param("desde") Instant desde, @Param("hasta") Instant hasta);
}
