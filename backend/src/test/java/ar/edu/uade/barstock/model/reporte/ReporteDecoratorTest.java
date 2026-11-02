package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.Ejemplos;
import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.OrigenComanda;
import ar.edu.uade.barstock.model.inventario.AjusteStock;
import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import ar.edu.uade.barstock.model.precio.PrecioNormal;
import ar.edu.uade.barstock.model.receta.Receta;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Decorator: cada envoltorio agrega una sección sin cambiar los indicadores del reporte base. */
class ReporteDecoratorTest {
    private static final Instant AHORA = Instant.parse("2026-11-02T23:00:00Z");
    private DatosReporte datos;

    @BeforeEach
    void preparar() {
        Insumo gin = Ejemplos.gin();
        Insumo tonica = Ejemplos.tonica();
        Receta ginTonic = Ejemplos.ginTonic(gin, tonica);
        Receta shot = new Receta(Ejemplos.BAR, "Shot de gin", "", "🥃", new BigDecimal("4000"), Map.of(gin, 40));

        Comanda primera = despachada(1, Map.of(ginTonic, 1));
        Comanda segunda = despachada(2, Map.of(ginTonic, 2, shot, 1));
        Comanda devuelta = despachada(3, Map.of(shot, 1));
        devuelta.anular("Trago devuelto", AHORA);

        Auditoria cierre = Auditoria.registrar(Ejemplos.BAR, 1, "Encargado", List.of(
                new AjusteStock(gin.getId(), "Gin", UnidadMedida.ML, new BigDecimal("700"), new BigDecimal("680"),
                        gin.getCostoUnitario())), AHORA);
        MovimientoStock compra = MovimientoStock.compra(tonica, new BigDecimal("1000"), "1 envase", AHORA);
        datos = new DatosReporte(List.of(primera, segunda, devuelta), List.of(compra), List.of(cierre));
    }

    @Test
    void elReporteBaseCalculaVentasCostosComprasMermasYDevoluciones() {
        ContenidoReporte contenido = new ReporteBase(datos).generar();
        Indicadores i = contenido.getIndicadores();

        assertThat(contenido.getComposicion()).containsExactly("ReporteBase");
        assertThat(contenido.getSecciones()).isEmpty();
        assertThat(i.ventasNetas()).isEqualByComparingTo("33400"); // 9800 + 19600 + 4000
        assertThat(i.comandasCobradas()).isEqualTo(2);
        assertThat(i.unidadesVendidas()).isEqualTo(4);
        // Costo de lo consumido, incluida la devolución: 1746,50 × 3 + 1013,20 × 2.
        assertThat(i.costoInsumos()).isEqualByComparingTo("7265.90");
        assertThat(i.contribucion()).isEqualByComparingTo("26134.10");
        assertThat(i.devoluciones()).isEqualByComparingTo("4000");
        assertThat(i.comandasDevueltas()).isEqualTo(1);
        assertThat(i.compras()).isEqualByComparingTo("3200");
        assertThat(i.mermas()).isEqualByComparingTo("506.60");
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
    void lasTendenciasYLasMermasSeAgreganComoSeccionesPropias() {
        ContenidoReporte contenido = new ReporteConMermas(new ReporteConTendencias(new ReporteBase(datos), datos), datos)
                .generar();
        var tendencia = (ReporteConTendencias.Tendencia) contenido.getSecciones().get(0).datos();
        assertThat(tendencia.comparable()).isTrue();
        assertThat(tendencia.primeraMitad()).isEqualByComparingTo("9800");
        assertThat(tendencia.segundaMitad()).isEqualByComparingTo("23600");

        @SuppressWarnings("unchecked")
        var mermas = (List<ReporteConMermas.MermaInsumo>) contenido.getSecciones().get(1).datos();
        assertThat(mermas).singleElement().satisfies(merma -> {
            assertThat(merma.cantidadPerdida()).isEqualByComparingTo("20");
            assertThat(merma.costo()).isEqualByComparingTo("506.60");
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

    private static Comanda despachada(int numero, Map<Receta, Integer> pedido) {
        Comanda comanda = Comanda.registrar(Ejemplos.BAR, numero, OrigenComanda.BARTENDER, null, new PrecioNormal(),
                pedido, AHORA);
        comanda.preparar(AHORA);
        comanda.despachar(AHORA.plusSeconds(numero));
        return comanda;
    }
}
