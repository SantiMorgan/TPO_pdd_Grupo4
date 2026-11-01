package ar.edu.uade.barstock.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ArranqueIT {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway flyway;

    @Test
    void arrancaConPostgresMigraElEsquemaYExponeLaApi() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("3");
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.schemata WHERE schema_name = 'barstock'",
                Integer.class)).isEqualTo(1);
        for (String ruta : new String[]{"/api", "/api/"}) {
            var estado = http.getForEntity(ruta, String.class);
            assertThat(estado.getStatusCode().value()).isEqualTo(200);
            assertThat(estado.getBody()).contains("BarStock Solutions");
        }
        var salud = http.getForEntity("/api/health", String.class);
        assertThat(salud.getStatusCode().value()).isEqualTo(200);
        assertThat(salud.getBody()).contains("\"status\":\"UP\"");
    }
}
