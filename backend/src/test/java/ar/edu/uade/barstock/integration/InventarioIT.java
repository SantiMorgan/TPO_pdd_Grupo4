package ar.edu.uade.barstock.integration;

import ar.edu.uade.barstock.controller.dto.InsumoResponse;
import ar.edu.uade.barstock.controller.dto.StockInsumoResponse;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventarioIT {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void limpiarBaseDePrueba() {
        jdbc.update("DELETE FROM barstock.insumo");
    }

    @Test
    void creaConsultaEditaDaDeBajaYReactivaConPersistenciaReal() {
        var creacion = http.postForEntity("/api/insumos", producto(" gin-01 ", "750"), InsumoResponse.class);
        assertThat(creacion.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        InsumoResponse insumo = creacion.getBody();
        assertThat(insumo).isNotNull();
        assertThat(insumo.codigo()).isEqualTo("GIN-01");
        assertThat(insumo.stockActual()).isEqualByComparingTo("750");
        assertThat(insumo.stockActual().scale()).isZero();
        assertThat(insumo.puntoReorden().scale()).isZero();
        String ruta = "/api/insumos/" + insumo.id();
        assertThat(creacion.getHeaders().getLocation()).hasToString(ruta);

        assertThat(http.getForEntity(ruta, InsumoResponse.class).getBody()).isEqualTo(insumo);
        assertThat(http.getForObject("/api/insumos", InsumoResponse[].class)).containsExactly(insumo);
        StockInsumoResponse stock = http.getForObject(ruta + "/stock", StockInsumoResponse.class);
        assertThat(stock).isNotNull();
        assertThat(stock.stockActual()).isEqualByComparingTo("750");

        var edicion = http.exchange(ruta, HttpMethod.PUT, new HttpEntity<>(Map.of(
                "nombre", "Gin premium", "costoUnitario", new BigDecimal("0.25"),
                "puntoReorden", new BigDecimal("800"))), InsumoResponse.class);
        assertThat(edicion.getStatusCode()).isEqualTo(HttpStatus.OK);
        InsumoResponse editado = http.getForObject(ruta, InsumoResponse.class);
        assertThat(editado).isNotNull();
        assertThat(editado.nombre()).isEqualTo("Gin premium");
        assertThat(editado.costoUnitario()).isEqualByComparingTo("0.25");
        assertThat(editado.stockActual()).isEqualByComparingTo("750");
        assertThat(editado.necesitaReposicion()).isTrue();

        for (int i = 0; i < 2; i++) {
            assertThat(http.exchange(ruta, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class).getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);
        }
        assertThat(http.getForObject("/api/insumos", InsumoResponse[].class)).isEmpty();
        assertThat(http.getForObject("/api/insumos?activo=false", InsumoResponse[].class))
                .extracting(InsumoResponse::id).containsExactly(insumo.id());
        assertThat(http.getForObject(ruta, InsumoResponse.class).activo()).isFalse();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM barstock.insumo WHERE id = ?", Integer.class, insumo.id()))
                .isEqualTo(1);

        assertThat(http.exchange(ruta, HttpMethod.PUT, new HttpEntity<>(Map.of(
                "nombre", "No permitido", "costoUnitario", 1, "puntoReorden", 0)), ProblemDetail.class)
                .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        for (int i = 0; i < 2; i++) {
            var alta = http.postForEntity(ruta + "/alta", null, InsumoResponse.class);
            assertThat(alta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(alta.getBody().activo()).isTrue();
            assertThat(alta.getBody().id()).isEqualTo(insumo.id());
            assertThat(alta.getBody().stockActual()).isEqualByComparingTo("750");
        }
        assertThat(http.getForObject("/api/insumos", InsumoResponse[].class)).hasSize(1);
    }

    @Test
    void codigoSeReservaTrasLaBajaYNoDistingueMayusculas() {
        var creado = http.postForEntity("/api/insumos", producto("GIN", "1"), InsumoResponse.class);
        http.delete("/api/insumos/" + creado.getBody().id());
        var repetido = http.postForEntity("/api/insumos", producto(" gin ", "1"), ProblemDetail.class);
        assertThat(repetido.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM barstock.insumo", Integer.class)).isEqualTo(1);
    }

    @Test
    void rechazaStockNegativoSinCrearRegistrosYLaBaseTambienLoImpide() {
        var rechazo = http.postForEntity("/api/insumos", producto("GIN", "-0.001"), ProblemDetail.class);
        assertThat(rechazo.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM barstock.insumo", Integer.class)).isZero();

        var creado = http.postForEntity("/api/insumos", producto("GIN", "0"), InsumoResponse.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThatThrownBy(() -> jdbc.update("UPDATE barstock.insumo SET stock_actual = -1 WHERE id = ?",
                creado.getBody().id())).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jdbc.queryForObject("SELECT stock_actual FROM barstock.insumo WHERE id = ?",
                BigDecimal.class, creado.getBody().id())).isEqualByComparingTo("0");
    }

    @ParameterizedTest
    @EnumSource(UnidadMedida.class)
    void cantidadesSonEnterasEnApiYBaseParaAmbasUnidades(UnidadMedida unidad) {
        var datos = new HashMap<>(producto("ENTERO", "750"));
        datos.put("unidadMedida", unidad.name());
        var creado = http.postForEntity("/api/insumos", datos, JsonNode.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(creado.getBody().get("stockActual").isIntegralNumber()).isTrue();
        assertThat(creado.getBody().get("puntoReorden").isIntegralNumber()).isTrue();
        String ruta = "/api/insumos/" + creado.getBody().get("id").asText();
        var consultado = http.getForObject(ruta, JsonNode.class);
        assertThat(consultado.get("stockActual").isIntegralNumber()).isTrue();
        assertThat(consultado.get("puntoReorden").isIntegralNumber()).isTrue();

        datos.put("codigo", "FRACCION");
        datos.put("stockInicial", new BigDecimal("0.5"));
        assertThat(http.postForEntity("/api/insumos", datos, ProblemDetail.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        datos.put("stockInicial", BigDecimal.ONE);
        datos.put("puntoReorden", new BigDecimal("0.5"));
        assertThat(http.postForEntity("/api/insumos", datos, ProblemDetail.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(http.exchange(ruta, HttpMethod.PUT, new HttpEntity<>(Map.of(
                "nombre", "Gin", "costoUnitario", new BigDecimal("0.25"),
                "puntoReorden", new BigDecimal("0.5"))), ProblemDetail.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThatThrownBy(() -> jdbc.update("UPDATE barstock.insumo SET stock_actual = 0.5"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE barstock.insumo SET punto_reorden = 0.5"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jdbc.queryForObject("SELECT stock_actual FROM barstock.insumo", BigDecimal.class))
                .isEqualByComparingTo("750");
        assertThat(jdbc.queryForObject("SELECT punto_reorden FROM barstock.insumo", BigDecimal.class))
                .isEqualByComparingTo("100");
    }

    @Test
    void dosCreacionesConcurrentesConElMismoCodigoSoloCreanUnProducto() throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            var primera = ejecutor.submit(() -> crearAlMismoTiempo(inicio));
            var segunda = ejecutor.submit(() -> crearAlMismoTiempo(inicio));
            inicio.countDown();
            assertThat(java.util.List.of(primera.get(30, TimeUnit.SECONDS), segunda.get(30, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(HttpStatus.CREATED, HttpStatus.CONFLICT);
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM barstock.insumo", Integer.class)).isEqualTo(1);
    }

    private HttpStatus crearAlMismoTiempo(CountDownLatch inicio) throws InterruptedException {
        assertThat(inicio.await(10, TimeUnit.SECONDS)).isTrue();
        ResponseEntity<String> respuesta = http.postForEntity("/api/insumos", producto("GIN", "1"), String.class);
        return HttpStatus.valueOf(respuesta.getStatusCode().value());
    }

    private Map<String, Object> producto(String codigo, String stockInicial) {
        return Map.of("codigo", codigo, "nombre", "Gin", "unidadMedida", "ML",
                "stockInicial", new BigDecimal(stockInicial), "costoUnitario", new BigDecimal("0.10"),
                "puntoReorden", new BigDecimal("100"));
    }
}
