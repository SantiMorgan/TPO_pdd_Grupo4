package ar.edu.uade.barstock.model.precio;

import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.establecimiento.ModoPrecio;
import ar.edu.uade.barstock.model.establecimiento.PoliticaPrecio;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SelectorEstrategiaPrecioTest {
    private static final ZoneId BUENOS_AIRES = ZoneId.of("America/Argentina/Buenos_Aires");

    private final Establecimiento bar = new Establecimiento("AMBAR", "Bar Ámbar", "Palermo", "Gorriti 4850", "");

    @Test
    void enModoAutomaticoUsaHappyHourDentroDelHorario() {
        var vigente = selectorALas("22:30:00Z").vigente(bar); // 19:30 en Buenos Aires
        assertThat(vigente.estrategia()).isInstanceOf(PrecioHappyHour.class);
        assertThat(vigente.motivo()).isEqualTo("Horario de Happy Hour 18:00–21:00");
        assertThat(vigente.hora()).isEqualTo(LocalTime.of(19, 30));
        assertThat(vigente.horaSimulada()).isFalse();
    }

    @Test
    void fueraDelHorarioUsaElPrecioNormal() {
        var vigente = selectorALas("01:00:00Z").vigente(bar); // 22:00 en Buenos Aires
        assertThat(vigente.estrategia()).isInstanceOf(PrecioNormal.class);
        assertThat(vigente.motivo()).isEqualTo("Horario normal");
    }

    @Test
    void elModoFijadoPorElDuenoGanaSobreLaHora() {
        var selector = selectorALas("22:30:00Z");
        bar.cambiarPoliticaPrecio(politica(ModoPrecio.NORMAL, LocalTime.of(18, 0), LocalTime.of(21, 0), null));
        assertThat(selector.vigente(bar).estrategia()).isInstanceOf(PrecioNormal.class);
        var fuera = selectorALas("01:00:00Z");
        bar.cambiarPoliticaPrecio(politica(ModoPrecio.HAPPY_HOUR, LocalTime.of(18, 0), LocalTime.of(21, 0), null));
        assertThat(fuera.vigente(bar).estrategia()).isInstanceOf(PrecioHappyHour.class);
        assertThat(fuera.vigente(bar).motivo()).isEqualTo("Fijado por el dueño");
    }

    @Test
    void laHoraSimuladaReemplazaALaRealParaLaDemo() {
        bar.cambiarPoliticaPrecio(politica(ModoPrecio.AUTOMATICO, LocalTime.of(18, 0), LocalTime.of(21, 0),
                LocalTime.of(19, 0)));
        var vigente = selectorALas("15:00:00Z").vigente(bar); // 12:00 real
        assertThat(vigente.estrategia()).isInstanceOf(PrecioHappyHour.class);
        assertThat(vigente.horaSimulada()).isTrue();
        assertThat(vigente.hora()).isEqualTo(LocalTime.of(19, 0));
    }

    @Test
    void elHorarioDeHappyHourPuedeCruzarLaMedianoche() {
        bar.cambiarPoliticaPrecio(politica(ModoPrecio.AUTOMATICO, LocalTime.of(23, 0), LocalTime.of(1, 0), null));
        assertThat(selectorALas("03:30:00Z").vigente(bar).estrategia()).isInstanceOf(PrecioHappyHour.class); // 00:30
        assertThat(selectorALas("04:00:00Z").vigente(bar).estrategia()).isInstanceOf(PrecioNormal.class); // 01:00
    }

    @Test
    void ofreceLasDosEstrategiasConLosParametrosDelBarParaCompararlas() {
        assertThat(selectorALas("12:00:00Z").todas(bar)).extracting(EstrategiaPrecio::nombre)
                .containsExactly("Precio normal", "Happy Hour −20 %");
    }

    private static SelectorEstrategiaPrecio selectorALas(String horaUtc) {
        return new SelectorEstrategiaPrecio(Clock.fixed(Instant.parse("2026-11-02T" + horaUtc), BUENOS_AIRES));
    }

    private static PoliticaPrecio politica(ModoPrecio modo, LocalTime inicio, LocalTime fin, LocalTime simulada) {
        return new PoliticaPrecio(modo, inicio, fin, new BigDecimal("20"), new BigDecimal("50"), simulada);
    }
}
