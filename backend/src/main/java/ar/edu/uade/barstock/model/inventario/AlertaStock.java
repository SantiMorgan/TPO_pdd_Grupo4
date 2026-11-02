package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.establecimiento.ConfiguracionAlertas;
import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.notificacion.MensajeAlerta;
import ar.edu.uade.barstock.model.notificacion.Notificacion;
import ar.edu.uade.barstock.model.notificacion.NotificacionFactory;
import ar.edu.uade.barstock.model.notificacion.TipoNotificacion;
import ar.edu.uade.barstock.model.persistence.EstablecimientoRepository;
import ar.edu.uade.barstock.model.traza.TrazaPatrones;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Observador concreto: cuando un insumo cruza su punto de reposición, pide a NotificacionFactory
 * un canal por cada medio configurado en el establecimiento y envía el aviso.
 */
@Component
@Order(2)
public class AlertaStock implements ObservadorStock {
    private final EstablecimientoRepository establecimientos;
    private final NotificacionFactory fabrica;
    private final TrazaPatrones traza;

    public AlertaStock(EstablecimientoRepository establecimientos, NotificacionFactory fabrica, TrazaPatrones traza) {
        this.establecimientos = establecimientos;
        this.fabrica = fabrica;
        this.traza = traza;
    }

    @Override
    public void actualizar(EventoStock evento) {
        if (!evento.cruzoPuntoDeReposicion()) {
            return;
        }
        Establecimiento establecimiento = establecimientos.findById(evento.establecimientoId()).orElse(null);
        if (establecimiento == null) {
            return;
        }
        String unidad = evento.unidad().simbolo();
        String texto = "%s: quedan %s %s; el punto de reposición es %s %s.".formatted(evento.nombreInsumo(),
                evento.stockActual().toPlainString(), unidad, evento.puntoReorden().toPlainString(), unidad);
        traza.registrar(establecimiento.getId(), "Observer",
                "AlertaStock detectó que " + evento.nombreInsumo() + " cruzó su punto de reposición");
        ConfiguracionAlertas alertas = establecimiento.getAlertas();
        for (TipoNotificacion canal : alertas.getCanales()) {
            Notificacion notificacion = fabrica.crear(canal);
            notificacion.enviar(new MensajeAlerta(establecimiento.getId(), evento.insumoId(),
                    alertas.destinatarioPara(canal), texto));
            traza.registrar(establecimiento.getId(), "Simple Factory",
                    "NotificacionFactory.crear(" + canal + ") → " + notificacion.getClass().getSimpleName());
        }
    }
}
