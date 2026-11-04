package ar.edu.uade.barstock.model.notificacion;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** Simple Factory: un único punto que decide qué clase concreta crear según el tipo. */
class NotificacionFactoryTest {
    private final BitacoraNotificaciones bitacora = mock(BitacoraNotificaciones.class);
    private final NotificacionFactory fabrica = new NotificacionFactory(bitacora);
    private final MensajeAlerta mensaje = new MensajeAlerta(UUID.randomUUID(), UUID.randomUUID(), "destino", "texto");

    @Test
    void creaLaClaseConcretaQueCorrespondeACadaTipo() {
        assertThat(fabrica.crear(TipoNotificacion.PANEL)).isInstanceOf(NotificacionPanel.class);
        assertThat(fabrica.crear(TipoNotificacion.EMAIL)).isInstanceOf(NotificacionEmail.class);
        assertThat(fabrica.crear(TipoNotificacion.SMS)).isInstanceOf(NotificacionSms.class);
        assertThat(fabrica.crear(TipoNotificacion.WHATSAPP)).isInstanceOf(NotificacionWhatsApp.class);
    }

    @ParameterizedTest
    @EnumSource(TipoNotificacion.class)
    void cadaProductoInformaSuTipoYRegistraElEnvio(TipoNotificacion tipo) {
        Notificacion notificacion = fabrica.crear(tipo);
        notificacion.enviar(mensaje);
        assertThat(notificacion.tipo()).isEqualTo(tipo);
        verify(bitacora).registrar(mensaje, tipo,
                tipo == TipoNotificacion.PANEL ? ResultadoNotificacion.MOSTRADA : ResultadoNotificacion.SIMULADA);
    }

    @Test
    void rechazaUnTipoNulo() {
        assertThatThrownBy(() -> fabrica.crear(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
