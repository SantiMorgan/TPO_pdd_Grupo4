package ar.edu.uade.barstock.model.notificacion;

import ar.edu.uade.barstock.model.persistence.RegistroNotificacionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Guarda cada aviso emitido. Las alertas se envían después del commit del descuento,
 * por eso el registro usa una transacción propia (REQUIRES_NEW).
 */
@Component
public class BitacoraNotificaciones {
    private final RegistroNotificacionRepository repositorio;
    private final TransactionTemplate transaccionPropia;
    private final Clock reloj;

    public BitacoraNotificaciones(RegistroNotificacionRepository repositorio, PlatformTransactionManager transacciones,
                                  Clock reloj) {
        this.repositorio = repositorio;
        this.transaccionPropia = new TransactionTemplate(transacciones);
        this.transaccionPropia.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.reloj = reloj;
    }

    public void registrar(MensajeAlerta mensaje, TipoNotificacion canal, ResultadoNotificacion resultado) {
        RegistroNotificacion registro = new RegistroNotificacion(mensaje, canal, resultado, Instant.now(reloj));
        transaccionPropia.executeWithoutResult(estado -> repositorio.save(registro));
    }

    public List<RegistroNotificacion> ultimas(UUID establecimientoId, int limite) {
        return repositorio.findByEstablecimientoIdOrderByFechaDesc(establecimientoId,
                PageRequest.of(0, Math.max(1, Math.min(limite, 100))));
    }
}
