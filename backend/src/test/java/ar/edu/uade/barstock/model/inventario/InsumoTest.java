package ar.edu.uade.barstock.model.inventario;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InsumoTest {
    @Test
    void creaConCantidadesEnterasYConservaDecimalesSoloEnElCosto() {
        Insumo insumo = new Insumo(" gin-01 ", " Gin ", UnidadMedida.ML,
                new BigDecimal("750"), new BigDecimal("0.10"), new BigDecimal("100"));

        assertThat(insumo.getId()).isNotNull();
        assertThat(insumo.getCodigo()).isEqualTo("GIN-01");
        assertThat(insumo.getNombre()).isEqualTo("Gin");
        assertThat(insumo.getStockActual()).isEqualTo(new BigDecimal("750"));
        assertThat(insumo.getCostoUnitario()).isEqualTo(new BigDecimal("0.10"));
        assertThat(insumo.getPuntoReorden()).isEqualTo(new BigDecimal("100"));
        assertThat(insumo.isActivo()).isTrue();
        assertThat(insumo.necesitaReposicion()).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rechazaCodigoONombreVacio(String texto) {
        assertThatThrownBy(() -> crear(texto, "Gin", "1", "1", "0"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> crear("GIN", texto, "1", "1", "0"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("numerosInvalidos")
    void rechazaNegativosExcesoDePrecisionYNulos(BigDecimal stock, BigDecimal costo, BigDecimal punto) {
        assertThatThrownBy(() -> new Insumo("GIN", "Gin", UnidadMedida.ML, stock, costo, punto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static Stream<Arguments> numerosInvalidos() {
        BigDecimal uno = BigDecimal.ONE;
        return Stream.of(
                Arguments.of(new BigDecimal("-0.001"), uno, uno),
                Arguments.of(uno, new BigDecimal("-0.01"), uno),
                Arguments.of(uno, uno, new BigDecimal("-0.001")),
                Arguments.of(new BigDecimal("0.0001"), uno, uno),
                Arguments.of(new BigDecimal("1.5"), uno, uno),
                Arguments.of(new BigDecimal("1.0"), uno, uno),
                Arguments.of(uno, new BigDecimal("0.001"), uno),
                Arguments.of(uno, uno, new BigDecimal("0.0001")),
                Arguments.of(uno, uno, new BigDecimal("1.5")),
                Arguments.of(uno, uno, new BigDecimal("1.0")),
                Arguments.of(new BigDecimal("1000000000000"), uno, uno),
                Arguments.of(uno, new BigDecimal("10000000000"), uno),
                Arguments.of(uno, uno, new BigDecimal("1000000000000")),
                Arguments.of(null, uno, uno), Arguments.of(uno, null, uno), Arguments.of(uno, uno, null));
    }

    @Test
    void rechazaTextosLargosYUnidadAusente() {
        assertThatThrownBy(() -> crear("C".repeat(51), "Gin", "1", "1", "0"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> crear("GIN", "N".repeat(151), "1", "1", "0"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Insumo("GIN", "Gin", null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void permiteCeroYLosValoresMaximos() {
        Insumo cero = crear("CERO", "Sin stock", "0", "0", "0");
        assertThat(cero.necesitaReposicion()).isTrue();
        Insumo maximo = crear("MAX", "Máximo", "999999999999", "9999999999.99", "999999999999");
        assertThat(maximo.getStockActual()).isEqualByComparingTo("999999999999");
        assertThat(maximo.necesitaReposicion()).isTrue();
    }

    @Test
    void editaDatosSinModificarCodigoUnidadNiStock() {
        Insumo insumo = crear("GIN", "Gin", "750", "0.10", "100");
        insumo.editar("Gin premium", new BigDecimal("0.25"), new BigDecimal("200"));

        assertThat(insumo.getNombre()).isEqualTo("Gin premium");
        assertThat(insumo.getCostoUnitario()).isEqualByComparingTo("0.25");
        assertThat(insumo.getPuntoReorden()).isEqualByComparingTo("200");
        assertThat(insumo.getCodigo()).isEqualTo("GIN");
        assertThat(insumo.getUnidadMedida()).isEqualTo(UnidadMedida.ML);
        assertThat(insumo.getStockActual()).isEqualByComparingTo("750");
    }

    @Test
    void unaEdicionInvalidaNoModificaParcialmenteElInsumo() {
        Insumo insumo = crear("GIN", "Gin", "750", "1", "100");
        assertThatThrownBy(() -> insumo.editar("Otro", BigDecimal.TEN, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(insumo.getNombre()).isEqualTo("Gin");
        assertThat(insumo.getCostoUnitario()).isEqualByComparingTo("1");
        assertThat(insumo.getPuntoReorden()).isEqualByComparingTo("100");
    }

    @Test
    void bajaYAltaSonIdempotentesConservanStockYLaBajaImpideEditar() {
        Insumo insumo = crear("GIN", "Gin", "750", "1", "100");
        insumo.darDeBaja();
        insumo.darDeBaja();
        assertThat(insumo.isActivo()).isFalse();
        assertThatThrownBy(() -> insumo.editar("Otro", BigDecimal.ONE, BigDecimal.ZERO))
                .isInstanceOf(InsumoInactivoException.class);
        insumo.darDeAlta();
        insumo.darDeAlta();
        assertThat(insumo.isActivo()).isTrue();
        assertThat(insumo.getStockActual()).isEqualByComparingTo("750");
    }

    private Insumo crear(String codigo, String nombre, String stock, String costo, String punto) {
        return new Insumo(codigo, nombre, UnidadMedida.ML,
                new BigDecimal(stock), new BigDecimal(costo), new BigDecimal(punto));
    }
}
