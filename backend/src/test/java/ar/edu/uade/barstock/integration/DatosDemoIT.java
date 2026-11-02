package ar.edu.uade.barstock.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/** El perfil demo carga los dos bares del prototipo y no duplica datos al volver a migrar. */
@Testcontainers
@ActiveProfiles("demo")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DatosDemoIT {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway flyway;

    @Test
    void cargaLosDosBaresConSuMenuYEsIdempotente() {
        var bares = http.getForObject("/api/establecimientos", JsonNode.class);
        assertThat(bares.findValuesAsText("nombre")).containsExactly("Bar Ámbar", "La Esquina");
        String ambar = bares.get(0).get("id").asText();
        String esquina = bares.get(1).get("id").asText();
        assertThat(http.getForObject("/api/establecimientos/" + ambar + "/carta", JsonNode.class).get("items")).hasSize(4);
        assertThat(http.getForObject("/api/establecimientos/" + esquina + "/carta", JsonNode.class).get("items")).hasSize(3);

        int insumos = jdbc.queryForObject("SELECT count(*) FROM barstock.insumo", Integer.class);
        assertThat(insumos).isEqualTo(12);
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM barstock.insumo", Integer.class)).isEqualTo(insumos);
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("6");
    }
}
