package ar.edu.uade.barstock.facade;

import ar.edu.uade.barstock.model.Ejemplos;
import ar.edu.uade.barstock.model.auditoria.AuditoriaService;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.ComandaService;
import ar.edu.uade.barstock.model.comanda.OrigenComanda;
import ar.edu.uade.barstock.model.comanda.state.TransicionInvalidaException;
import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.establecimiento.EstablecimientoService;
import ar.edu.uade.barstock.model.inventario.Consumo;
import ar.edu.uade.barstock.model.inventario.Inventario;
import ar.edu.uade.barstock.model.inventario.InventarioService;
import ar.edu.uade.barstock.model.inventario.StockInsuficienteException;
import ar.edu.uade.barstock.model.notificacion.BitacoraNotificaciones;
import ar.edu.uade.barstock.model.precio.EstrategiaVigente;
import ar.edu.uade.barstock.model.precio.PrecioNormal;
import ar.edu.uade.barstock.model.precio.SelectorEstrategiaPrecio;
import ar.edu.uade.barstock.model.receta.CantidadReceta;
import ar.edu.uade.barstock.model.receta.Receta;
import ar.edu.uade.barstock.model.receta.RecetaService;
import ar.edu.uade.barstock.model.reporte.GeneradorReportes;
import ar.edu.uade.barstock.model.traza.TrazaPatrones;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Facade: orquesta los subsistemas en el orden correcto y no contiene reglas propias. */
class BarStockFacadeTest {
    private final EstablecimientoService establecimientos = mock(EstablecimientoService.class);
    private final Inventario inventario = mock(Inventario.class);
    private final RecetaService recetas = mock(RecetaService.class);
    private final ComandaService comandas = mock(ComandaService.class);
    private final SelectorEstrategiaPrecio precios = mock(SelectorEstrategiaPrecio.class);
    private final BarStockFacade facade = new BarStockFacade(establecimientos, mock(InventarioService.class),
            inventario, recetas, comandas, precios, mock(AuditoriaService.class), mock(GeneradorReportes.class),
            mock(BitacoraNotificaciones.class), new TrazaPatrones(Clock.systemUTC()));

    private final UUID bar = Ejemplos.BAR;
    private Receta ginTonic;
    private Comanda comanda;
    private List<Consumo> consumos;

    @BeforeEach
    void preparar() {
        ginTonic = Ejemplos.ginTonic(Ejemplos.gin(), Ejemplos.tonica());
        comanda = Comanda.registrar(bar, 1, OrigenComanda.BARTENDER, null, new PrecioNormal(),
                Map.of(ginTonic, 1), Instant.now());
        consumos = ginTonic.consumos(1);
    }

    @Test
    void despacharCambiaElEstadoYDespuesDescuentaLosConsumosDeLaReceta() {
        when(comandas.despachar(bar, comanda.getId())).thenReturn(comanda);
        when(recetas.consumosDe(bar, comanda.cantidadesPorReceta())).thenReturn(consumos);

        assertThat(facade.despacharComanda(bar, comanda.getId())).isSameAs(comanda);

        InOrder orden = inOrder(comandas, recetas, inventario);
        orden.verify(comandas).despachar(bar, comanda.getId());
        orden.verify(recetas).consumosDe(bar, comanda.cantidadesPorReceta());
        orden.verify(inventario).descontarTodo(bar, consumos, "Comanda #1");
    }

    @Test
    void siLaTransicionEsInvalidaNoSeTocaElInventario() {
        when(comandas.despachar(bar, comanda.getId())).thenThrow(new TransicionInvalidaException(comanda,
                comanda.getEstado(), "despachar"));
        assertThatThrownBy(() -> facade.despacharComanda(bar, comanda.getId()))
                .isInstanceOf(TransicionInvalidaException.class);
        verify(inventario, never()).descontarTodo(any(), anyList(), anyString());
    }

    @Test
    void registrarNumeraVerificaStockEligeLaEstrategiaYRegistra() {
        Establecimiento establecimiento = new Establecimiento("AMBAR", "Bar Ámbar", "Palermo", "");
        List<CantidadReceta> pedido = List.of(new CantidadReceta(ginTonic.getId(), 1));
        var vigente = new EstrategiaVigente(new PrecioNormal(), "Horario normal", LocalTime.NOON, false);
        when(establecimientos.bloquear(bar)).thenReturn(establecimiento);
        when(recetas.armarPedido(bar, pedido)).thenReturn(Map.of(ginTonic, 1));
        when(recetas.consumosDe(bar, pedido)).thenReturn(consumos);
        when(comandas.contarEnCurso(bar)).thenReturn(2L);
        when(precios.vigente(establecimiento, 2L)).thenReturn(vigente);
        when(comandas.registrar(establecimiento, OrigenComanda.CLIENTE, "Mesa 2", vigente.estrategia(),
                Map.of(ginTonic, 1))).thenReturn(comanda);

        assertThat(facade.registrarComanda(bar, OrigenComanda.CLIENTE, "Mesa 2", pedido)).isSameAs(comanda);

        InOrder orden = inOrder(establecimientos, recetas, inventario, precios, comandas);
        orden.verify(establecimientos).bloquear(bar);
        orden.verify(recetas).armarPedido(bar, pedido);
        orden.verify(inventario).verificarDisponibilidad(bar, consumos);
        orden.verify(precios).vigente(establecimiento, 2L);
        orden.verify(comandas).registrar(establecimiento, OrigenComanda.CLIENTE, "Mesa 2", vigente.estrategia(),
                Map.of(ginTonic, 1));
    }

    @Test
    void sinStockSuficienteNoSeRegistraLaComanda() {
        List<CantidadReceta> pedido = List.of(new CantidadReceta(ginTonic.getId(), 50));
        when(establecimientos.bloquear(bar)).thenReturn(new Establecimiento("AMBAR", "Bar Ámbar", "Palermo", ""));
        when(recetas.consumosDe(bar, pedido)).thenReturn(ginTonic.consumos(50));
        doThrow(new StockInsuficienteException(List.of()))
                .when(inventario).verificarDisponibilidad(bar, ginTonic.consumos(50));

        assertThatThrownBy(() -> facade.registrarComanda(bar, OrigenComanda.CLIENTE, null, pedido))
                .isInstanceOf(StockInsuficienteException.class);
        verify(comandas, never()).registrar(any(), any(), any(), any(), any());
    }
}
