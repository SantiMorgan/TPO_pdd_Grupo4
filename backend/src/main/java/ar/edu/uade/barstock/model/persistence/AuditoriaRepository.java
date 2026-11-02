package ar.edu.uade.barstock.model.persistence;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, UUID> {
    List<Auditoria> findByEstablecimientoIdOrderByNumeroDesc(UUID establecimientoId, Pageable pagina);

    List<Auditoria> findByEstablecimientoId(UUID establecimientoId);
}
