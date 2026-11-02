package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.Ejemplos;
import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.OrigenComanda;
import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.establecimiento.Turno;
import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import ar.edu.uade.barstock.model.precio.PrecioNormal;
import ar.edu.uade.barstock.model.receta.Receta;
import ar.edu.uade.barstock.model.traza.TrazaPatrones;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** El generador filtra por jornada (de 6:00 a 5:59) y por turno según el horario de cada bar. */
class GeneradorReportesTest {
    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDate JORNADA = LocalDate.of(2026, 11, 2);

    private final Establecimiento bar = Ejemplos.bar(); // mediodía 12–16 y noche 20–2
    private List<Comanda> comandas;
    private GeneradorReportes generador;

    @BeforeEach
    void preparar() {
        Receta ginTonic = Ejemplos.ginTonic(Ejemplos.gin(), Ejemplos.tonica());
        comandas = List.of(
                despachada(1, ginTonic, LocalDateTime.of(2026, 11, 2, 13, 0)),  // mediodía del 2/11
                despachada(2, ginTonic, LocalDateTime.of(2026, 11, 2, 23, 0)),  // noche del 2/11
                despachada(3, ginTonic, LocalDateTime.of(2026, 11, 3, 2, 0)),   // madrugada: sigue siendo el 2/11
                despachada(4, ginTonic, LocalDateTime.of(2026, 11, 3, 13, 0))); // ya es la jornada del 3/11
        Clock reloj = Clock.fixed(Instant.parse("2026-11-03T08:30:00Z"), ZONA); // 3/11 a las 5:30
        generador = new GeneradorReportes(new FuenteEnMemoria(), new TrazaPatrones(reloj), reloj);
    }

    @Test
    void elReporteDiarioJuntaMediodiaYNocheIncluidaLaMadrugada() {
        ReporteDiario diario = generador.generarDiario(List.of(bar), JORNADA);

        assertThat(diario.total().comandasCobradas()).isEqualTo(3);
        assertThat(diario.total().ventasNetas()).isEqualByComparingTo("29400");
        assertThat(diario.turnos()).extracting(ReporteDiario.ResumenTurno::turno).containsExactly(Turno.MEDIODIA, Turno.NOCHE);
        assertThat(diario.turnos().get(0).indicadores().comandasCobradas()).isEqualTo(1);
        assertThat(diario.turnos().get(1).indicadores().comandasCobradas()).isEqualTo(2);
        assertThat(diario.desde()).isEqualTo(LocalDateTime.of(2026, 11, 2, 6, 0).atZone(ZONA).toInstant());
        assertThat(diario.ranking()).singleElement().satisfies(b -> assertThat(b.unidades()).isEqualTo(3));
    }

    @Test
    void elFiltroDeTurnoYDePeriodoSeAplicaAntesDeLosDecoradores() {
        var noche = generador.generar(List.of(bar), Periodo.de(JORNADA), Turno.NOCHE, List.of(TipoSeccion.TENDENCIAS));
        assertThat(noche.getIndicadores().comandasCobradas()).isEqualTo(2);
        assertThat(noche.getTurno()).isEqualTo(Turno.NOCHE);

        var dosDias = generador.generar(List.of(bar), new Periodo(JORNADA, JORNADA.plusDays(1)), null, List.of());
        assertThat(dosDias.getIndicadores().comandasCobradas()).isEqualTo(4);
    }

    @Test
    void antesDeLasSeisTodaviaEsLaJornadaAnterior() {
        assertThat(generador.jornadaActual()).isEqualTo(JORNADA);
    }

    private static Comanda despachada(int numero, Receta receta, LocalDateTime cuando) {
        Instant instante = cuando.atZone(ZONA).toInstant();
        Comanda comanda = Comanda.registrar(Ejemplos.BAR, numero, OrigenComanda.BARTENDER, null, new PrecioNormal(),
                Map.of(receta, 1), instante);
        comanda.preparar(instante);
        comanda.despachar(instante);
        return comanda;
    }

    private final class FuenteEnMemoria implements FuenteDatosReporte {
        @Override
        public List<Comanda> comandasDespachadas(Collection<UUID> establecimientos, Instant desde, Instant hasta) {
            return comandas.stream().filter(c -> !c.getDespachadaEn().isBefore(desde) && c.getDespachadaEn().isBefore(hasta))
                    .toList();
        }

        @Override
        public List<MovimientoStock> compras(Collection<UUID> establecimientos, Instant desde, Instant hasta) {
            return List.of();
        }

        @Override
        public List<Auditoria> auditorias(Collection<UUID> establecimientos, Instant desde, Instant hasta) {
            return List.of();
        }
    }
}
