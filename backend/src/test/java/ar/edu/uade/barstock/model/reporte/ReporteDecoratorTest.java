package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.Ejemplos;
import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.OrigenComanda;
import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.inventario.AjusteStock;
import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import ar.edu.uade.barstock.model.precio.PrecioNormal;
import ar.edu.uade.barstock.model.receta.Receta;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Decorator: cada envoltorio agrega una sección sin cambiar los indicadores del reporte base. */
class ReporteDecoratorTest {
    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final Instant AHORA = Instant.parse("2026-11-02T23:00:00Z"); // 20:00 del 2/11, turno noche
    private static final Instant AYER = Instant.parse("2026-11-01T23:30:00Z");
    private Receta ginTonic;
    private Comanda segunda;
    private DatosReporte datos;

    @BeforeEach
    void preparar() {
        Establecimiento bar = Ejemplos.bar();
        Insumo gin = Ejemplos.gin();
        Insumo tonica = Ejemplos.tonica();
        ginTonic = Ejemplos.ginTonic(gin, tonica);
        Receta shot = new Receta(Ejemplos.BAR, "Shot de gin", "", "🥃", new BigDecimal("4000"), Map.of(gin, 40));

        Comanda primera = despachada(1, Map.of(ginTonic, 1), AHORA);
        segunda = despachada(2, Map.of(ginTonic, 2, shot, 1), AHORA);
        Comanda devuelta = despachada(3, Map.of(shot, 1), AHORA);
        devuelta.devolver(devuelta.getItems().get(0).getId(), 1, "Trago devuelto", AHORA);
        Comanda deAyer = despachada(4, Map.of(ginTonic, 1), AYER);

        Auditoria cierre = Auditoria.registrar(Ejemplos.BAR, 1, "Encargado", List.of(
                new AjusteStock(gin.getId(), "Gin", UnidadMedida.ML, 750, new BigDecimal("700"), new BigDecimal("680"),
                        gin.getCostoUnitario(), "Se rompió una copa")), AHORA);
        MovimientoStock compra = MovimientoStock.compra(tonica, new BigDecimal("1000"), "1 envase", AHORA);
        datos = new DatosReporte(Periodo.de(LocalDate.of(2026, 11, 2)), null, List.of(bar),
                new CalendarioJornadas(ZONA, List.of(bar)), List.of(primera, segunda, devuelta), List.of(deAyer),
                List.of(compra), List.of(cierre));
    }

    @Test
    void elReporteBaseCalculaVentasCostosComprasMermasYDevoluciones() {
        ContenidoReporte contenido = new ReporteBase(datos).generar();
        Indicadores i = contenido.getIndicadores();

        assertThat(contenido.getComposicion()).containsExactly("ReporteBase");
        assertThat(contenido.getSecciones()).isEmpty();
        assertThat(contenido.getEstablecimientos()).containsExactly("Bar Ámbar");
        assertThat(i.ventasNetas()).isEqualByComparingTo("33400"); // 9800 + 19600 + 4000
        assertThat(i.comandasCobradas()).isEqualTo(2);
        assertThat(i.unidadesVendidas()).isEqualTo(4);
        // Costo de lo consumido, incluida la devolución: 1746,50 × 3 + 1013,20 × 2.
        assertThat(i.costoInsumos()).isEqualByComparingTo("7265.90");
        assertThat(i.contribucion()).isEqualByComparingTo("26134.10");
        assertThat(i.devoluciones()).isEqualByComparingTo("4000");
        assertThat(i.tragosDevueltos()).isEqualTo(1);
        assertThat(i.compras()).isEqualByComparingTo("3200");
        assertThat(i.mermas()).isEqualByComparingTo("506.60");
    }

    @Test
    void unaDevolucionParcialDescuentaSoloEseTragoDeLasVentas() {
        segunda.devolver(segunda.getItems().stream().filter(item -> item.getRecetaId().equals(ginTonic.getId()))
                .findFirst().orElseThrow().getId(), 1, "Estaba muy dulce", AHORA);
        Indicadores i = new ReporteBase(datos).generar().getIndicadores();

        assertThat(i.ventasNetas()).isEqualByComparingTo("23600");
        assertThat(i.comandasCobradas()).isEqualTo(2);
        assertThat(i.unidadesVendidas()).isEqualTo(3);
        assertThat(i.devoluciones()).isEqualByComparingTo("13800");
        assertThat(i.tragosDevueltos()).isEqualTo(2);
        assertThat(i.costoInsumos()).isEqualByComparingTo("7265.90"); // el trago devuelto igual consumió insumos
    }

    @Test
    void losDecoradoresSeApilanEnElOrdenEnQueSeEnvuelven() {
        Reporte reporte = new ReporteConMargen(new ReporteConGrafico(new ReporteBase(datos), datos), datos);
        ContenidoReporte contenido = reporte.generar();

        assertThat(contenido.getComposicion()).containsExactly("ReporteBase", "ReporteConGrafico", "ReporteConMargen");
        assertThat(contenido.getSecciones()).extracting(SeccionReporte::tipo).containsExactly("GRAFICO", "MARGEN");
        assertThat(contenido.getIndicadores()).isEqualTo(new ReporteBase(datos).generar().getIndicadores());
    }

    @Test
    void elRankingOrdenaLosTragosPorUnidadesVendidas() {
        var seccion = new ReporteConGrafico(new ReporteBase(datos), datos).generar().getSecciones().get(0);
        @SuppressWarnings("unchecked")
        var barras = (List<ReporteConGrafico.Barra>) seccion.datos();
        assertThat(barras).extracting(ReporteConGrafico.Barra::trago).containsExactly("Gin Tonic", "Shot de gin");
        assertThat(barras.get(0).unidades()).isEqualTo(3);
        assertThat(barras.get(0).participacion()).isEqualByComparingTo("75.0");
    }

    @Test
    void laTendenciaComparaConElPeriodoAnteriorDeIgualDuracion() {
        var seccion = new ReporteConTendencias(new ReporteBase(datos), datos).generar().getSecciones().get(0);
        var tendencia = (ReporteConTendencias.Tendencia) seccion.datos();

        assertThat(tendencia.periodo()).isEqualTo(Periodo.de(LocalDate.of(2026, 11, 2)));
        assertThat(tendencia.periodoAnterior()).isEqualTo(Periodo.de(LocalDate.of(2026, 11, 1)));
        assertThat(tendencia.ventasActual()).isEqualByComparingTo("33400");
        assertThat(tendencia.ventasAnterior()).isEqualByComparingTo("9800");
        assertThat(tendencia.variacionPorcentaje()).isEqualByComparingTo("240.8");
        assertThat(tendencia.porJornada()).singleElement().satisfies(dia -> {
            assertThat(dia.jornada()).isEqualTo(LocalDate.of(2026, 11, 2));
            assertThat(dia.comandas()).isEqualTo(2);
        });
        assertThat(tendencia.porEstablecimiento()).singleElement()
                .satisfies(bar -> assertThat(bar.nombre()).isEqualTo("Bar Ámbar"));
        assertThat(tendencia.porTrago()).first().satisfies(trago -> {
            assertThat(trago.trago()).isEqualTo("Gin Tonic");
            assertThat(trago.unidadesActual()).isEqualTo(3);
            assertThat(trago.unidadesAnterior()).isEqualTo(1);
        });
    }

    @Test
    void lasMermasMuestranLasBotellasPerdidasYLosComentarios() {
        var seccion = new ReporteConMermas(new ReporteBase(datos), datos).generar().getSecciones().get(0);
        @SuppressWarnings("unchecked")
        var mermas = (List<ReporteConMermas.MermaInsumo>) seccion.datos();
        assertThat(mermas).singleElement().satisfies(merma -> {
            assertThat(merma.establecimiento()).isEqualTo("Bar Ámbar");
            assertThat(merma.cantidadPerdida()).isEqualByComparingTo("20");
            assertThat(merma.envasesPerdidos()).isEqualByComparingTo("0.03"); // 20 ml de una botella de 750
            assertThat(merma.costo()).isEqualByComparingTo("506.60");
            assertThat(merma.comentarios()).containsExactly("Se rompió una copa");
        });
    }

    @Test
    void cadaTipoDeSeccionSabeQueDecoradorAplicar() {
        Reporte reporte = new ReporteBase(datos);
        for (TipoSeccion tipo : TipoSeccion.values()) {
            reporte = tipo.decorar(reporte, datos);
        }
        assertThat(reporte.generar().getComposicion()).containsExactly("ReporteBase", "ReporteConGrafico",
                "ReporteConTendencias", "ReporteConMermas", "ReporteConMargen");
    }

    private static Comanda despachada(int numero, Map<Receta, Integer> pedido, Instant cuando) {
        Comanda comanda = Comanda.registrar(Ejemplos.BAR, numero, OrigenComanda.BARTENDER, null, new PrecioNormal(),
                pedido, cuando);
        comanda.preparar(cuando);
        comanda.despachar(cuando.plusSeconds(numero));
        return comanda;
    }
}
