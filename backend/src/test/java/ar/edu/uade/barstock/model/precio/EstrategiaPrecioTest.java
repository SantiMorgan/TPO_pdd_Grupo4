package ar.edu.uade.barstock.model.precio;

import ar.edu.uade.barstock.model.Ejemplos;
import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.receta.Receta;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Patrón Strategy: el mismo trago con tres algoritmos de precio intercambiables. */
class EstrategiaPrecioTest {
    private final Receta ginTonic = Ejemplos.ginTonic(Ejemplos.gin(), Ejemplos.tonica());

    @Test
    void precioNormalEsElPrecioBase() {
        assertThat(new PrecioNormal().calcularPrecio(ginTonic)).isEqualByComparingTo("9800");
    }

    @Test
    void happyHourAplicaElDescuentoCuandoElMargenLoPermite() {
        var estrategia = new PrecioHappyHour(new BigDecimal("20"), new BigDecimal("50"));
        assertThat(ginTonic.calcularCosto()).isEqualByComparingTo("1746.50");
        assertThat(estrategia.calcularPrecio(ginTonic)).isEqualByComparingTo("7840.00");
        assertThat(estrategia.limitadoPorMargen(ginTonic)).isFalse();
        assertThat(estrategia.nombre()).isEqualTo("Happy Hour −20 %");
    }

    @Test
    void happyHourNuncaBajaDelCostoMasElMargenMinimo() {
        var estrategia = new PrecioHappyHour(new BigDecimal("90"), new BigDecimal("50"));
        // 9800 × 10 % = 980, pero el piso es 1746,50 × 1,5 = 2619,75.
        assertThat(estrategia.calcularPrecio(ginTonic)).isEqualByComparingTo("2619.75");
        assertThat(estrategia.limitadoPorMargen(ginTonic)).isTrue();
    }

    @Test
    void happyHourNuncaSubeElPrecioPorEncimaDelBase() {
        Insumo gin = Ejemplos.gin();
        Receta barata = new Receta(Ejemplos.BAR, "Shot", "", "🥃", new BigDecimal("1000"), Map.of(gin, 50));
        assertThat(new PrecioHappyHour(new BigDecimal("20"), new BigDecimal("50")).calcularPrecio(barata))
                .isEqualByComparingTo("1000");
    }

    @Test
    void demandaAltaAgregaElRecargo() {
        var estrategia = new PrecioDemandaAlta(new BigDecimal("15"));
        assertThat(estrategia.calcularPrecio(ginTonic)).isEqualByComparingTo("11270.00");
        assertThat(estrategia.nombre()).isEqualTo("Demanda alta +15 %");
    }

    @Test
    void lasEstrategiasSonIntercambiablesDetrasDeLaMismaInterfaz() {
        List<EstrategiaPrecio> estrategias = List.of(new PrecioNormal(),
                new PrecioHappyHour(new BigDecimal("20"), new BigDecimal("50")), new PrecioDemandaAlta(new BigDecimal("15")));
        assertThat(estrategias).extracting(e -> e.calcularPrecio(ginTonic))
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("9800"), new BigDecimal("7840"), new BigDecimal("11270"));
        assertThat(estrategias).extracting(EstrategiaPrecio::codigo).containsExactly("NORMAL", "HAPPY_HOUR", "DEMANDA_ALTA");
    }
}
