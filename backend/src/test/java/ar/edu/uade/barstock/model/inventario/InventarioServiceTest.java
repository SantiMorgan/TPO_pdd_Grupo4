package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.persistence.InsumoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventarioServiceTest {
    private final InsumoRepository repositorio = mock(InsumoRepository.class);
    private final InventarioService servicio = new InventarioService(repositorio);
    private Insumo insumo;

    @BeforeEach
    void preparar() {
        insumo = new Insumo("GIN", "Gin", UnidadMedida.ML,
                new BigDecimal("750"), BigDecimal.ONE, BigDecimal.TEN);
    }

    @Test
    void creaConCodigoNormalizadoYPersistenciaInmediata() {
        when(repositorio.saveAndFlush(any(Insumo.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        Insumo creado = servicio.crear(" gin ", "Gin", UnidadMedida.ML,
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO);
        assertThat(creado.getCodigo()).isEqualTo("GIN");
        verify(repositorio).existsByCodigo("GIN");
        verify(repositorio).saveAndFlush(creado);
    }

    @Test
    void rechazaCodigoDuplicadoInclusoCuandoExisteInactivo() {
        when(repositorio.existsByCodigo("GIN")).thenReturn(true);
        assertThatThrownBy(() -> servicio.crear(" gin ", "Otro gin", UnidadMedida.ML,
                BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TEN))
                .isInstanceOf(CodigoInsumoDuplicadoException.class);
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void stockNegativoNoLlegaAlRepositorio() {
        assertThatThrownBy(() -> servicio.crear("GIN", "Gin", UnidadMedida.ML,
                new BigDecimal("-1"), BigDecimal.ONE, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void consultaPorIdYFiltraActivosEInactivos() {
        when(repositorio.findById(insumo.getId())).thenReturn(Optional.of(insumo));
        when(repositorio.findByActivoOrderByNombreAscIdAsc(true)).thenReturn(List.of(insumo));
        when(repositorio.findByActivoOrderByNombreAscIdAsc(false)).thenReturn(List.of());
        assertThat(servicio.consultar(insumo.getId())).isSameAs(insumo);
        assertThat(servicio.listar(true)).containsExactly(insumo);
        assertThat(servicio.listar(false)).isEmpty();
    }

    @Test
    void operacionesSobreInexistenteDevuelvenErrorDeDominio() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> servicio.consultar(id)).isInstanceOf(InsumoNoEncontradoException.class);
        assertThatThrownBy(() -> servicio.editar(id, "Gin", BigDecimal.ONE, BigDecimal.ZERO))
                .isInstanceOf(InsumoNoEncontradoException.class);
        assertThatThrownBy(() -> servicio.darDeBaja(id)).isInstanceOf(InsumoNoEncontradoException.class);
        assertThatThrownBy(() -> servicio.darDeAlta(id)).isInstanceOf(InsumoNoEncontradoException.class);
    }

    @Test
    void editaConBloqueoYConservaStock() {
        when(repositorio.buscarParaActualizar(insumo.getId())).thenReturn(Optional.of(insumo));
        Insumo editado = servicio.editar(insumo.getId(), "Gin nuevo", new BigDecimal("2.50"), BigDecimal.ZERO);
        assertThat(editado.getNombre()).isEqualTo("Gin nuevo");
        assertThat(editado.getStockActual()).isEqualByComparingTo("750");
        verify(repositorio).buscarParaActualizar(insumo.getId());
    }

    @Test
    void bajaNoBorraYAltaRecuperaElMismoRegistro() {
        when(repositorio.buscarParaActualizar(insumo.getId())).thenReturn(Optional.of(insumo));
        servicio.darDeBaja(insumo.getId());
        assertThat(insumo.isActivo()).isFalse();
        verify(repositorio, never()).delete(any());
        assertThat(servicio.darDeAlta(insumo.getId())).isSameAs(insumo);
        assertThat(insumo.isActivo()).isTrue();
        assertThat(insumo.getStockActual()).isEqualByComparingTo("750");
    }
}
