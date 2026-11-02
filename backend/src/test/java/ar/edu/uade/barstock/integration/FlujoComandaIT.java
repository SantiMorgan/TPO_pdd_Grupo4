package ar.edu.uade.barstock.integration;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/** Casos felices e infelices del flujo registrar → preparar → despachar con PostgreSQL real. */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlujoComandaIT {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;

    private String bar;
    private String gin;
    private String tonica;
    private String ginTonic;

    @BeforeEach
    void prepararBar() {
        for (String tabla : List.of("notificacion", "detalle_auditoria", "auditoria", "movimiento_stock", "item_comanda",
                "comanda", "ingrediente_receta", "receta", "insumo", "establecimiento")) {
            jdbc.update("DELETE FROM barstock." + tabla);
        }
        String id = http.postForObject("/api/establecimientos",
                Map.of("codigo", "IT", "nombre", "Bar de prueba", "zona", "CABA"), JsonNode.class).get("id").asText();
        bar = "/api/establecimientos/" + id;
        cambiarModoDePrecio("NORMAL");
        put(bar + "/alertas", Map.of("canales", List.of("PANEL", "EMAIL"), "email", "encargado@it.example"));
        gin = crearInsumo("GIN", "900", "25.33", "300");
        tonica = crearInsumo("TONICA", "3000", "3.20", "800");
        ginTonic = http.postForObject(bar + "/recetas", Map.of("nombre", "Gin Tonic", "precioBase", 9800,
                "ingredientes", List.of(Map.of("insumoId", gin, "cantidad", 50), Map.of("insumoId", tonica, "cantidad", 150))),
                JsonNode.class).get("id").asText();
    }

    @Test
    void flujoCompletoDescuentaExactamenteLoDeLaRecetaYNoPermiteDespacharDosVeces() {
        var carta = http.getForObject(bar + "/carta", JsonNode.class);
        assertThat(carta.get("estrategia").get("codigo").asText()).isEqualTo("NORMAL");
        assertThat(carta.get("items").get(0).get("precio").decimalValue()).isEqualByComparingTo("9800");

        var registro = http.postForEntity(bar + "/comandas", pedido("CLIENTE", 2), JsonNode.class);
        assertThat(registro.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String comanda = bar + "/comandas/" + registro.getBody().get("id").asText();
        assertThat(registro.getBody().get("estado").asText()).isEqualTo("REGISTRADA");
        assertThat(registro.getBody().get("total").decimalValue()).isEqualByComparingTo("19600");
        assertThat(stock(gin)).isEqualByComparingTo("900");

        assertThat(post(comanda + "/preparar").getBody().get("estado").asText()).isEqualTo("EN_PREPARACION");
        var despacho = post(comanda + "/despachar");
        assertThat(despacho.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(despacho.getBody().get("estado").asText()).isEqualTo("DESPACHADA");
        assertThat(stock(gin)).isEqualByComparingTo("800");
        assertThat(stock(tonica)).isEqualByComparingTo("2700");

        assertThat(post(comanda + "/despachar").getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(stock(gin)).isEqualByComparingTo("800");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM barstock.movimiento_stock WHERE tipo = 'CONSUMO'",
                Integer.class)).isEqualTo(2);
    }

    @Test
    void siFaltaUnInsumoAlDespacharNoSeDescuentaNingunoYLaComandaNoCambiaDeEstado() {
        jdbc.update("UPDATE barstock.insumo SET stock_actual = 100 WHERE id = ?::uuid", gin);
        String primera = registrarYPreparar(2);
        String segunda = registrarYPreparar(2);

        assertThat(post(primera + "/despachar").getStatusCode()).isEqualTo(HttpStatus.OK);
        var rechazo = post(segunda + "/despachar");

        assertThat(rechazo.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(rechazo.getBody().get("faltantes").get(0).asText()).contains("faltan 100 ml");
        assertThat(stock(gin)).isEqualByComparingTo("0");
        assertThat(stock(tonica)).isEqualByComparingTo("2700");
        assertThat(http.getForObject(segunda, JsonNode.class).get("estado").asText()).isEqualTo("EN_PREPARACION");
        var traza = http.getForObject(bar + "/traza", JsonNode.class).toString();
        assertThat(traza).contains("Comanda #1: En preparación → Despachada")
                .doesNotContain("Comanda #2: En preparación → Despachada");
    }

    @Test
    void dosDespachosSimultaneosDeLaMismaComandaDescuentanUnaSolaVez() throws Exception {
        String comanda = registrarYPreparar(1);
        CountDownLatch inicio = new CountDownLatch(1);
        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            var primero = ejecutor.submit(() -> despacharAlMismoTiempo(comanda, inicio));
            var segundo = ejecutor.submit(() -> despacharAlMismoTiempo(comanda, inicio));
            inicio.countDown();
            assertThat(List.of(primero.get(30, TimeUnit.SECONDS), segundo.get(30, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(HttpStatus.OK, HttpStatus.CONFLICT);
        }
        assertThat(stock(gin)).isEqualByComparingTo("850");
    }

    @Test
    void alCruzarElPuntoDeReposicionSeAvisaUnaVezPorCadaCanalDespuesDelCommit() {
        jdbc.update("UPDATE barstock.insumo SET stock_actual = 320 WHERE id = ?::uuid", gin);
        post(registrarYPreparar(1) + "/despachar");
        post(registrarYPreparar(1) + "/despachar");

        var avisos = http.getForObject(bar + "/notificaciones", JsonNode.class);
        assertThat(avisos).hasSize(2);
        assertThat(avisos.findValuesAsText("canal")).containsExactlyInAnyOrder("PANEL", "EMAIL");
        assertThat(avisos.findValuesAsText("mensaje")).allMatch(m -> m.startsWith("Gin: quedan 270 ml"));
    }

    @Test
    void elPrecioSeFijaAlRegistrarAunqueDespuesCambieLaEstrategia() {
        cambiarModoDePrecio("HAPPY_HOUR");
        String comanda = registrarYPreparar(1);
        cambiarModoDePrecio("NORMAL");
        var despachada = post(comanda + "/despachar").getBody();

        assertThat(despachada.get("total").decimalValue()).isEqualByComparingTo("7840");
        assertThat(despachada.get("estrategiaPrecio").asText()).isEqualTo("Happy Hour −20 %");
        assertThat(http.getForObject(bar + "/reportes", JsonNode.class).get("indicadores").get("ventasNetas")
                .decimalValue()).isEqualByComparingTo("7840");
    }

    @Test
    void laDevolucionRevierteElIngresoPeroNoReponeLosInsumos() {
        String comanda = registrarYPreparar(1);
        post(comanda + "/despachar");
        var anulada = http.postForObject(comanda + "/anular", Map.of("motivo", "Trago devuelto"), JsonNode.class);

        assertThat(anulada.get("estado").asText()).isEqualTo("ANULADA");
        assertThat(anulada.get("estadoAlAnular").asText()).isEqualTo("DESPACHADA");
        assertThat(stock(gin)).isEqualByComparingTo("850");
        var indicadores = http.getForObject(bar + "/reportes", JsonNode.class).get("indicadores");
        assertThat(indicadores.get("ventasNetas").decimalValue()).isEqualByComparingTo("0");
        assertThat(indicadores.get("devoluciones").decimalValue()).isEqualByComparingTo("9800");
        assertThat(post(comanda + "/anular").getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void elRecuentoFisicoAjustaElStockYLosDecoradoresAgreganSusSecciones() {
        post(registrarYPreparar(1) + "/despachar");
        var auditoria = http.postForEntity(bar + "/auditorias", Map.of("responsable", "Encargado",
                "conteos", List.of(Map.of("insumoId", gin, "stockFisico", 830))), JsonNode.class);

        assertThat(auditoria.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(auditoria.getBody().get("mermaTotal").decimalValue()).isEqualByComparingTo("506.60");
        assertThat(stock(gin)).isEqualByComparingTo("830");

        var reporte = http.getForObject(bar + "/reportes?secciones=GRAFICO,MERMAS", JsonNode.class);
        assertThat(reporte.get("composicion").toString())
                .isEqualTo("[\"ReporteBase\",\"ReporteConGrafico\",\"ReporteConMermas\"]");
        assertThat(reporte.get("secciones").get(0).get("datos").get(0).get("trago").asText()).isEqualTo("Gin Tonic");
        assertThat(reporte.get("secciones").get(1).get("datos").get(0).get("costo").decimalValue())
                .isEqualByComparingTo("506.60");
        assertThat(reporte.get("indicadores").get("mermas").decimalValue()).isEqualByComparingTo("506.60");
    }

    @Test
    void noSeRegistranPedidosQueHoyNoAlcanzanNiDeRecetasFueraDelMenu() {
        assertThat(http.postForEntity(bar + "/comandas", pedido("CLIENTE", 19), JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        http.delete(bar + "/recetas/" + ginTonic);
        assertThat(http.postForEntity(bar + "/comandas", pedido("CLIENTE", 1), JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM barstock.comanda", Integer.class)).isZero();
    }

    @Test
    void editarUnaRecetaReemplazaSusIngredientesYRecalculaElCosto() {
        String vermut = crearInsumo("VERMUT", "1000", "10.30", "300");
        var edicion = http.exchange(bar + "/recetas/" + ginTonic, HttpMethod.PUT, new HttpEntity<>(Map.of(
                "nombre", "Gin Tonic", "precioBase", 9900, "ingredientes", List.of(
                        Map.of("insumoId", vermut, "cantidad", 20), Map.of("insumoId", gin, "cantidad", 60)))), JsonNode.class);

        assertThat(edicion.getStatusCode()).isEqualTo(HttpStatus.OK);
        var receta = http.getForObject(bar + "/recetas/" + ginTonic, JsonNode.class);
        assertThat(receta.findValuesAsText("insumo")).containsExactly("Vermut", "Gin");
        assertThat(receta.get("costo").decimalValue()).isEqualByComparingTo("1725.80"); // 20 × 10,30 + 60 × 25,33
        assertThat(jdbc.queryForObject("SELECT count(*) FROM barstock.ingrediente_receta WHERE receta_id = ?::uuid",
                Integer.class, ginTonic)).isEqualTo(2);

        var repetido = http.exchange(bar + "/recetas/" + ginTonic, HttpMethod.PUT, new HttpEntity<>(Map.of(
                "nombre", "Gin Tonic", "precioBase", 9900, "ingredientes", List.of(
                        Map.of("insumoId", gin, "cantidad", 20), Map.of("insumoId", gin, "cantidad", 60)))), JsonNode.class);
        assertThat(repetido.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private String registrarYPreparar(int cantidad) {
        var registro = http.postForObject(bar + "/comandas", pedido("BARTENDER", cantidad), JsonNode.class);
        String comanda = bar + "/comandas/" + registro.get("id").asText();
        post(comanda + "/preparar");
        return comanda;
    }

    private HttpStatus despacharAlMismoTiempo(String comanda, CountDownLatch inicio) throws InterruptedException {
        assertThat(inicio.await(10, TimeUnit.SECONDS)).isTrue();
        return HttpStatus.valueOf(http.postForEntity(comanda + "/despachar", null, String.class).getStatusCode().value());
    }

    private Map<String, Object> pedido(String origen, int cantidad) {
        return Map.of("origen", origen, "cliente", "Mesa 1",
                "items", List.of(Map.of("recetaId", ginTonic, "cantidad", cantidad)));
    }

    private String crearInsumo(String codigo, String stock, String costo, String reorden) {
        return http.postForObject(bar + "/insumos", Map.of("codigo", codigo, "nombre", codigo.charAt(0)
                        + codigo.substring(1).toLowerCase(), "unidadMedida", "ML", "stockInicial", new BigDecimal(stock),
                "costoUnitario", new BigDecimal(costo), "puntoReorden", new BigDecimal(reorden), "contenidoEnvase", 750),
                JsonNode.class).get("id").asText();
    }

    private void cambiarModoDePrecio(String modo) {
        put(bar + "/politica-precio", Map.of("modo", modo, "inicioHappyHour", "18:00", "finHappyHour", "21:00",
                "descuentoHappyHour", 20, "incrementoDemandaAlta", 15, "umbralDemandaAlta", 4, "margenMinimo", 50));
    }

    private BigDecimal stock(String insumo) {
        return http.getForObject(bar + "/insumos/" + insumo + "/stock", JsonNode.class).get("stockActual").decimalValue();
    }

    private ResponseEntity<JsonNode> post(String ruta) {
        return http.postForEntity(ruta, null, JsonNode.class);
    }

    private void put(String ruta, Object cuerpo) {
        assertThat(http.exchange(ruta, HttpMethod.PUT, new HttpEntity<>(cuerpo), JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }
}
