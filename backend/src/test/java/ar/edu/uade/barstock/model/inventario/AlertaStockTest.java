package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.establecimiento.ConfiguracionAlertas;
import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.notificacion.MensajeAlerta;
import ar.edu.uade.barstock.model.notificacion.Notificacion;
import ar.edu.uade.barstock.model.notificacion.NotificacionFactory;
import ar.edu.uade.barstock.model.notificacion.TipoNotificacion;
import ar.edu.uade.barstock.model.persistence.EstablecimientoRepository;
import ar.edu.uade.barstock.model.traza.TrazaPatrones;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** El observador concreto pide a la fábrica un canal por cada medio configurado. */
class AlertaStockTest {
    private final EstablecimientoRepository establecimientos = mock(EstablecimientoRepository.class);
    private final NotificacionFactory fabrica = mock(NotificacionFactory.class);
    private final Notificacion panel = mock(Notificacion.class);
    private final Notificacion email = mock(Notificacion.class);
    private final TrazaPatrones traza = new TrazaPatrones(Clock.systemUTC());
    private final AlertaStock alerta = new AlertaStock(establecimientos, fabrica, traza);
    private Establecimiento bar;

    @BeforeEach
    void preparar() {
        bar = new Establecimiento("AMBAR", "Bar Ámbar", "Palermo", "");
        bar.cambiarAlertas(new ConfiguracionAlertas(EnumSet.of(TipoNotificacion.PANEL, TipoNotificacion.EMAIL),
                "encargado@ambar.example", null));
        when(establecimientos.findById(bar.getId())).thenReturn(Optional.of(bar));
        when(fabrica.crear(TipoNotificacion.PANEL)).thenReturn(panel);
        when(fabrica.crear(TipoNotificacion.EMAIL)).thenReturn(email);
    }

    @Test
    void alCruzarElPuntoDeReposicionAvisaPorCadaCanalConfigurado() {
        alerta.actualizar(evento("270", "240"));

        var mensaje = ArgumentCaptor.forClass(MensajeAlerta.class);
        verify(email).enviar(mensaje.capture());
        assertThat(mensaje.getValue().destinatario()).isEqualTo("encargado@ambar.example");
        assertThat(mensaje.getValue().texto()).isEqualTo("Campari: quedan 240 ml; el punto de reposición es 250 ml.");
        verify(panel).enviar(org.mockito.ArgumentMatchers.any());
        assertThat(traza.consultar(bar.getId())).extracting(e -> e.patron())
                .contains("Observer", "Simple Factory");
    }

    @Test
    void noAvisaSiElStockSigueArribaOSiYaEstabaPorDebajo() {
        alerta.actualizar(evento("900", "870"));
        alerta.actualizar(evento("240", "210"));
        alerta.actualizar(evento("240", "990"));
        verifyNoInteractions(fabrica);
    }

    private EventoStock evento(String anterior, String actual) {
        return new EventoStock(bar.getId(), UUID.randomUUID(), "Campari", UnidadMedida.ML,
                new BigDecimal(anterior), new BigDecimal(actual), new BigDecimal("250"));
    }
}
