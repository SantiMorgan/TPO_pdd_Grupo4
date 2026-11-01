package ar.edu.uade.barstock.integration;

import java.math.BigDecimal;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class CantidadesEnterasMigracionIT {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private JdbcTemplate jdbc;

    @BeforeEach
    void prepararBaseAisladaEnVersionDos() {
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));
        flyway("2").clean();
        flyway("2").migrate();
    }

    @Test
    void migraEnterosExistentesSinDecimalesNiCambiosEnLosCostos() {
        insertar("750.000");
        flyway("3").migrate();

        BigDecimal stock = jdbc.queryForObject("SELECT stock_actual FROM barstock.insumo", BigDecimal.class);
        BigDecimal punto = jdbc.queryForObject("SELECT punto_reorden FROM barstock.insumo", BigDecimal.class);
        assertThat(stock).isEqualTo(new BigDecimal("750"));
        assertThat(punto).isEqualTo(new BigDecimal("100"));
        assertThat(jdbc.queryForObject("SELECT costo_unitario FROM barstock.insumo", BigDecimal.class))
                .isEqualTo(new BigDecimal("0.25"));
    }

    @Test
    void rechazaMigracionDeFraccionesExistentesSinRedondearNiPerderDatos() {
        insertar("750.125");
        assertThatThrownBy(() -> flyway("3").migrate()).isInstanceOf(FlywayException.class);

        assertThat(jdbc.queryForObject("SELECT stock_actual FROM barstock.insumo", BigDecimal.class))
                .isEqualTo(new BigDecimal("750.125"));
        assertThat(flyway("2").info().current().getVersion().getVersion()).isEqualTo("2");
    }

    private void insertar(String stock) {
        jdbc.update("""
                INSERT INTO barstock.insumo
                    (id, codigo, nombre, unidad_medida, stock_actual, costo_unitario, punto_reorden)
                VALUES (?, 'GIN', 'Gin', 'ML', ?, 0.25, 100)
                """, UUID.randomUUID(), new BigDecimal(stock));
    }

    private Flyway flyway(String version) {
        return Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .defaultSchema("barstock").schemas("barstock")
                .cleanDisabled(false).target(version).load();
    }
}
