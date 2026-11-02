package ar.edu.uade.barstock.model.comanda.state;

import ar.edu.uade.barstock.model.Ejemplos;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.OrigenComanda;
import ar.edu.uade.barstock.model.precio.PrecioNormal;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Patrón State: cada estado habilita sus transiciones y rechaza el resto sin modificar la comanda. */
class EstadoComandaTest {
    private static final Instant AHORA = Instant.parse("2026-11-02T22:00:00Z");
    private Comanda comanda;

    @BeforeEach
    void registrar() {
        var receta = Ejemplos.ginTonic(Ejemplos.gin(), Ejemplos.tonica());
        comanda = Comanda.registrar(Ejemplos.BAR, 7, OrigenComanda.CLIENTE, " Mesa 4 ", new PrecioNormal(),
                Map.of(receta, 2), AHORA);
    }

    @Test
    void nuevaComandaQuedaRegistradaConPrecioFijadoYPermitePrepararOAnular() {
        assertThat(comanda.getEstado()).isSameAs(EstadosComanda.REGISTRADA);
        assertThat(comanda.accionesPermitidas()).containsExactlyInAnyOrder(AccionComanda.PREPARAR, AccionComanda.ANULAR);
        assertThat(comanda.getTotal()).isEqualByComparingTo("19600");
        assertThat(comanda.getCostoTotal()).isEqualByComparingTo("3493");
        assertThat(comanda.getCliente()).isEqualTo("Mesa 4");
        assertThat(comanda.getEstrategiaPrecio()).isEqualTo("Precio normal");
        assertThat(comanda.getEstado().enCurso()).isTrue();
    }

    @Test
    void cicloCompletoRegistraCadaMomentoYSoloPermiteDevolverAlFinal() {
        comanda.preparar(AHORA.plusSeconds(60));
        assertThat(comanda.getEstado()).isSameAs(EstadosComanda.EN_PREPARACION);
        assertThat(comanda.accionesPermitidas()).containsExactlyInAnyOrder(AccionComanda.DESPACHAR, AccionComanda.ANULAR);

        comanda.despachar(AHORA.plusSeconds(120));
        assertThat(comanda.getEstado()).isSameAs(EstadosComanda.DESPACHADA);
        assertThat(comanda.getPreparadaEn()).isEqualTo(AHORA.plusSeconds(60));
        assertThat(comanda.getDespachadaEn()).isEqualTo(AHORA.plusSeconds(120));
        assertThat(comanda.accionesPermitidas()).containsExactly(AccionComanda.ANULAR);
        assertThat(comanda.cuentaComoVenta()).isTrue();
        assertThat(comanda.getEstado().enCurso()).isFalse();
    }

    @Test
    void noSeDespachaSinPrepararNiSePreparaDosVeces() {
        assertThatThrownBy(() -> comanda.despachar(AHORA))
                .isInstanceOf(TransicionInvalidaException.class)
                .hasMessage("La comanda #7 está Registrada: no se puede despachar");
        assertThat(comanda.getEstado()).isSameAs(EstadosComanda.REGISTRADA);
        assertThat(comanda.getDespachadaEn()).isNull();

        comanda.preparar(AHORA);
        assertThatThrownBy(() -> comanda.preparar(AHORA)).isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void unaComandaDespachadaNoSeDespachaOtraVez() {
        comanda.preparar(AHORA);
        comanda.despachar(AHORA);
        assertThatThrownBy(() -> comanda.despachar(AHORA))
                .isInstanceOf(TransicionInvalidaException.class)
                .hasMessageContaining("Despachada");
    }

    @Test
    void anularDespuesDelDespachoEsUnaDevolucionQueDejaDeContarComoVenta() {
        comanda.preparar(AHORA);
        comanda.despachar(AHORA);
        comanda.anular("  El cliente lo devolvió ", AHORA.plusSeconds(300));

        assertThat(comanda.getEstado()).isSameAs(EstadosComanda.ANULADA);
        assertThat(comanda.getEstadoAlAnular()).isEqualTo("DESPACHADA");
        assertThat(comanda.getMotivoAnulacion()).isEqualTo("El cliente lo devolvió");
        assertThat(comanda.fueDevuelta()).isTrue();
        assertThat(comanda.cuentaComoVenta()).isFalse();
    }

    @Test
    void anuladaEsUnEstadoFinal() {
        comanda.anular("Pedido duplicado", AHORA);
        assertThat(comanda.fueDevuelta()).isFalse();
        assertThat(comanda.getEstadoAlAnular()).isEqualTo("REGISTRADA");
        assertThat(comanda.accionesPermitidas()).isEmpty();
        assertThatThrownBy(() -> comanda.preparar(AHORA)).isInstanceOf(TransicionInvalidaException.class);
        assertThatThrownBy(() -> comanda.despachar(AHORA)).isInstanceOf(TransicionInvalidaException.class);
        assertThatThrownBy(() -> comanda.anular("Otra vez", AHORA)).isInstanceOf(TransicionInvalidaException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "    ", "abc"})
    void anularExigeUnMotivoDeAlMenosCincoCaracteres(String motivo) {
        assertThatThrownBy(() -> comanda.anular(motivo, AHORA)).isInstanceOf(IllegalArgumentException.class);
        assertThat(comanda.getEstado()).isSameAs(EstadosComanda.REGISTRADA);
    }

    @Test
    void elConversorPersisteElCodigoYReconstruyeLaMismaInstancia() {
        var conversor = new EstadoComandaConverter();
        for (EstadoComanda estado : EstadosComanda.todos()) {
            String codigo = conversor.convertToDatabaseColumn(estado);
            assertThat(conversor.convertToEntityAttribute(codigo)).isSameAs(estado);
        }
        assertThat(EstadosComanda.enCurso()).containsExactly(EstadosComanda.REGISTRADA, EstadosComanda.EN_PREPARACION);
        assertThatThrownBy(() -> EstadosComanda.desde("PAGADA")).isInstanceOf(IllegalArgumentException.class);
    }
}
