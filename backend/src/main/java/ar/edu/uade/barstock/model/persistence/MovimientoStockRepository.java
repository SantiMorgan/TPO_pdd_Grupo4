package ar.edu.uade.barstock.model.persistence;

import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import ar.edu.uade.barstock.model.inventario.TipoMovimiento;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, UUID> {
    List<MovimientoStock> findByEstablecimientoIdOrderByFechaDesc(UUID establecimientoId, Pageable pagina);

    List<MovimientoStock> findByEstablecimientoIdAndTipo(UUID establecimientoId, TipoMovimiento tipo);
}
