package ar.edu.uade.barstock.model.persistence;

import ar.edu.uade.barstock.model.notificacion.RegistroNotificacion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroNotificacionRepository extends JpaRepository<RegistroNotificacion, UUID> {
    List<RegistroNotificacion> findByEstablecimientoIdOrderByFechaDesc(UUID establecimientoId, Pageable pagina);
}
