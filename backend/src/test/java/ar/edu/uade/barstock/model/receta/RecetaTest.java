package ar.edu.uade.barstock.model.receta;

import ar.edu.uade.barstock.model.Ejemplos;
import ar.edu.uade.barstock.model.inventario.Consumo;
import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecetaTest {
    private Insumo gin;
    private Insumo tonica;
    private Receta ginTonic;

    @BeforeEach
    void preparar() {
        gin = Ejemplos.gin();
        tonica = Ejemplos.tonica();
        ginTonic = Ejemplos.ginTonic(gin, tonica);
    }

    @Test
    void calculaElCostoTeoricoComoExpertoEnSusIngredientes() {
        assertThat(ginTonic.calcularCosto()).isEqualByComparingTo("1746.50");
    }

    @Test
    void traduceUnidadesVendidasAConsumosConSuUnidad() {
        assertThat(ginTonic.consumos(3)).containsExactly(
                new Consumo(gin.getId(), new BigDecimal("150"), UnidadMedida.ML),
                new Consumo(tonica.getId(), new BigDecimal("450"), UnidadMedida.ML));
        assertThatThrownBy(() -> ginTonic.consumos(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dejaDeEstarDisponibleSinStockConInsumoDadoDeBajaOFueraDelMenu() {
        assertThat(ginTonic.disponible()).isTrue();
        tonica.ajustarA(new BigDecimal("100"));
        assertThat(ginTonic.disponible()).isFalse();
        tonica.ajustarA(new BigDecimal("3000"));
        gin.darDeBaja();
        assertThat(ginTonic.disponible()).isFalse();
        gin.darDeAlta();
        ginTonic.quitarDelMenu();
        assertThat(ginTonic.disponible()).isFalse();
    }

    @Test
    void editarConservaLosIngredientesQueSiguenYQuitaLosDemas() {
        var ingredienteGin = ginTonic.getIngredientes().get(0);
        ginTonic.editar("Gin solo", "", "", new BigDecimal("7000"), Map.of(gin, 60));

        assertThat(ginTonic.getIngredientes()).singleElement().isSameAs(ingredienteGin);
        assertThat(ingredienteGin.getCantidad()).isEqualTo(60);
        assertThat(ginTonic.getIcono()).isEqualTo("🍹");
    }

    @Test
    void rechazaRecetasSinIngredientesConInsumosDeOtroBarOPrecioInvalido() {
        assertThatThrownBy(() -> new Receta(Ejemplos.BAR, "Vacía", "", "", BigDecimal.TEN, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        Insumo ajeno = new Insumo(UUID.randomUUID(), "GIN", "Gin", UnidadMedida.ML, BigDecimal.TEN, BigDecimal.ONE,
                BigDecimal.ONE, 750);
        assertThatThrownBy(() -> new Receta(Ejemplos.BAR, "Ajena", "", "", BigDecimal.TEN, Map.of(ajeno, 10)))
                .isInstanceOf(IllegalArgumentException.class);
        Map<Insumo, Integer> ingredientes = new LinkedHashMap<>();
        ingredientes.put(gin, 50);
        assertThatThrownBy(() -> new Receta(Ejemplos.BAR, "Gratis", "", "", BigDecimal.ZERO, ingredientes))
                .isInstanceOf(IllegalArgumentException.class);
        ingredientes.put(gin, 0);
        assertThatThrownBy(() -> new Receta(Ejemplos.BAR, "Sin gin", "", "", BigDecimal.TEN, ingredientes))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
