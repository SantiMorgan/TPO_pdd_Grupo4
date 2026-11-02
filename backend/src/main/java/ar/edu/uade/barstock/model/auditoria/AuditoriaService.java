package ar.edu.uade.barstock.model.auditoria;

import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.inventario.AjusteStock;
import ar.edu.uade.barstock.model.persistence.AuditoriaRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditoriaService {
    private final AuditoriaRepository auditorias;
    private final Clock reloj;

    public AuditoriaService(AuditoriaRepository auditorias, Clock reloj) {
        this.auditorias = auditorias;
        this.reloj = reloj;
    }

    /** El establecimiento debe llegar bloqueado para numerar el cierre sin repetirlo. */
    @Transactional
    public Auditoria registrar(Establecimiento establecimiento, String responsable, List<AjusteStock> ajustes) {
        Auditoria auditoria = Auditoria.registrar(establecimiento.getId(), establecimiento.asignarNumeroAuditoria(),
                responsable, ajustes, Instant.now(reloj));
        return auditorias.save(auditoria);
    }

    public List<Auditoria> listar(UUID establecimientoId, int limite) {
        return auditorias.findByEstablecimientoIdOrderByNumeroDesc(establecimientoId,
                PageRequest.of(0, Math.max(1, Math.min(limite, 100))));
    }
}
