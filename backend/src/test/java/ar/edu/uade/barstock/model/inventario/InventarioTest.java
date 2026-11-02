package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.Ejemplos;
import ar.edu.uade.barstock.model.persistence.InsumoRepository;
import ar.edu.uade.barstock.model.persistence.MovimientoStockRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Patrón Observer y descuento atómico, sin Spring ni base de datos (no hay transacción activa). */
class InventarioTest {
    private final InsumoRepository insumos = mock(InsumoRepository.class);
    private final MovimientoStockRepository movimientos = mock(MovimientoStockRepository.class);
    private final List<EventoStock> recibidos = new ArrayList<>();
    private final ObservadorStock observador = recibidos::add;
    private Inventario inventario;
    private Insumo gin;
    private Insumo tonica;

    @BeforeEach
    void preparar() {
        gin = Ejemplos.gin();
        tonica = Ejemplos.tonica();
        for (Insumo insumo : List.of(gin, tonica)) {
            when(insumos.buscarParaActualizar(insumo.getId())).thenReturn(Optional.of(insumo));
            when(insumos.findById(insumo.getId())).thenReturn(Optional.of(insumo));
        }
        when(movimientos.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        inventario = new Inventario(insumos, movimientos, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), List.of(observador));
    }

    @Test
    void descuentaTodosLosInsumosRegistraMovimientosYAvisaACadaObservador() {
        inventario.descontarTodo(Ejemplos.BAR, List.of(consumo(gin, 50), consumo(tonica, 150)), "Comanda #1");

        assertThat(gin.getStockActual()).isEqualByComparingTo("850");
        assertThat(tonica.getStockActual()).isEqualByComparingTo("2850");
        verify(movimientos, times(2)).save(any(MovimientoStock.class));
        assertThat(recibidos).extracting(EventoStock::nombreInsumo).containsExactlyInAnyOrder("Gin", "Tonica");
        assertThat(recibidos).allSatisfy(evento -> assertThat(evento.cruzoPuntoDeReposicion()).isFalse());
    }

    @Test
    void sumaLosConsumosDelMismoInsumoAntesDeValidar() {
        inventario.descontarTodo(Ejemplos.BAR, List.of(consumo(gin, 450), consumo(gin, 450)), "Dos tragos");

        assertThat(gin.getStockActual()).isEqualByComparingTo("0");
        var movimiento = ArgumentCaptor.forClass(MovimientoStock.class);
        verify(movimientos).save(movimiento.capture());
        assertThat(movimiento.getValue().getCantidad()).isEqualByComparingTo("-900");
        assertThat(recibidos).singleElement().satisfies(evento -> assertThat(evento.cruzoPuntoDeReposicion()).isTrue());
    }

    @Test
    void siFaltaUnInsumoNoDescuentaNingunoNiAvisa() {
        assertThatThrownBy(() -> inventario.descontarTodo(Ejemplos.BAR,
                List.of(consumo(gin, 50), consumo(tonica, 3001)), "Comanda #2"))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Tonica: faltan 1 ml")
                .satisfies(ex -> assertThat(((StockInsuficienteException) ex).getFaltantes()).hasSize(1));

        assertThat(gin.getStockActual()).isEqualByComparingTo("900");
        assertThat(tonica.getStockActual()).isEqualByComparingTo("3000");
        verify(movimientos, never()).save(any());
        assertThat(recibidos).isEmpty();
    }

    @Test
    void unObservadorQueFallaNoDeshaceElCambioNiImpideAvisarALosDemas() {
        List<EventoStock> segundo = new ArrayList<>();
        inventario.agregarObservador(evento -> {
            throw new IllegalStateException("SMTP caído");
        });
        inventario.agregarObservador(segundo::add);

        inventario.descontarTodo(Ejemplos.BAR, List.of(consumo(gin, 10)), "Prueba");

        assertThat(gin.getStockActual()).isEqualByComparingTo("890");
        assertThat(recibidos).hasSize(1);
        assertThat(segundo).hasSize(1);
    }

    @Test
    void unObservadorQuitadoDejaDeRecibirAvisos() {
        inventario.quitarObservador(observador);
        inventario.descontarTodo(Ejemplos.BAR, List.of(consumo(gin, 10)), "Prueba");
        assertThat(recibidos).isEmpty();
    }

    @Test
    void rechazaUnidadesIncompatiblesEInsumosDeOtroEstablecimiento() {
        var enUnidades = new Consumo(gin.getId(), BigDecimal.ONE, UnidadMedida.UNIDAD);
        assertThatThrownBy(() -> inventario.descontarTodo(Ejemplos.BAR, List.of(enUnidades), "Prueba"))
                .isInstanceOf(UnidadIncompatibleException.class);
        assertThatThrownBy(() -> inventario.descontarTodo(UUID.randomUUID(), List.of(consumo(gin, 1)), "Prueba"))
                .isInstanceOf(InsumoNoEncontradoException.class);
        assertThat(gin.getStockActual()).isEqualByComparingTo("900");
    }

    @Test
    void unInsumoDadoDeBajaNoSePuedeConsumirNiComprar() {
        tonica.darDeBaja();
        assertThatThrownBy(() -> inventario.descontarTodo(Ejemplos.BAR, List.of(consumo(tonica, 1)), "Prueba"))
                .isInstanceOf(InsumoInactivoException.class);
        assertThatThrownBy(() -> inventario.registrarCompra(Ejemplos.BAR, tonica.getId(), 1))
                .isInstanceOf(InsumoInactivoException.class);
    }

    @Test
    void laCompraIngresaEnvasesCompletosYValorizaElGasto() {
        MovimientoStock compra = inventario.registrarCompra(Ejemplos.BAR, gin.getId(), 2);

        assertThat(gin.getStockActual()).isEqualByComparingTo("2400");
        assertThat(compra.getTipo()).isEqualTo(TipoMovimiento.COMPRA);
        assertThat(compra.getCantidad()).isEqualByComparingTo("1500");
        assertThat(compra.getMonto()).isEqualByComparingTo("37995.00");
        assertThat(recibidos).singleElement().satisfies(evento -> {
            assertThat(evento.stockAnterior()).isEqualByComparingTo("900");
            assertThat(evento.stockActual()).isEqualByComparingTo("2400");
        });
        assertThatThrownBy(() -> inventario.registrarCompra(Ejemplos.BAR, gin.getId(), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elRecuentoAjustaAlValorFisicoYSoloRegistraLasDiferencias() {
        Map<UUID, BigDecimal> recuento = new LinkedHashMap<>();
        recuento.put(gin.getId(), new BigDecimal("880"));
        recuento.put(tonica.getId(), new BigDecimal("3000"));

        List<AjusteStock> ajustes = inventario.ajustarARecuento(Ejemplos.BAR, recuento, "Recuento #1");

        assertThat(ajustes).hasSize(2);
        assertThat(ajustes).filteredOn(a -> a.insumoId().equals(gin.getId())).singleElement()
                .satisfies(a -> assertThat(a.diferencia()).isEqualByComparingTo("-20"));
        assertThat(gin.getStockActual()).isEqualByComparingTo("880");
        verify(movimientos, times(1)).save(any());
        assertThat(recibidos).hasSize(1);
    }

    @Test
    void verificarDisponibilidadNoModificaNiBloquea() {
        inventario.verificarDisponibilidad(Ejemplos.BAR, List.of(consumo(gin, 900)));
        assertThatThrownBy(() -> inventario.verificarDisponibilidad(Ejemplos.BAR, List.of(consumo(gin, 901))))
                .isInstanceOf(StockInsuficienteException.class);
        assertThat(gin.getStockActual()).isEqualByComparingTo("900");
        verify(insumos, never()).buscarParaActualizar(any());
        verifyNoInteractions(movimientos);
    }

    private static Consumo consumo(Insumo insumo, int cantidad) {
        return new Consumo(insumo.getId(), BigDecimal.valueOf(cantidad), insumo.getUnidadMedida());
    }
}
