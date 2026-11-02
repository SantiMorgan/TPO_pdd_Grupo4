package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.error.ApiExceptionHandler;
import ar.edu.uade.barstock.facade.BarStockFacade;
import ar.edu.uade.barstock.model.inventario.CodigoInsumoDuplicadoException;
import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.inventario.InsumoInactivoException;
import ar.edu.uade.barstock.model.inventario.InsumoNoEncontradoException;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InventarioControllerTest {
    private static final String CREACION = """
            {"codigo":"GIN", "nombre":"Gin", "unidadMedida":"ML", "stockInicial":750,
             "costoUnitario":0.10, "puntoReorden":100}
            """;
    private static final String EDICION = """
            {"nombre":"Gin premium", "costoUnitario":0.25, "puntoReorden":150}
            """;
    private static final UUID EST = UUID.randomUUID();
    private static final String RUTA = "/api/establecimientos/" + EST + "/insumos";
    private final BarStockFacade facade = mock(BarStockFacade.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new InventarioController(facade))
            .setControllerAdvice(new ApiExceptionHandler()).build();
    private Insumo insumo;

    @BeforeEach
    void preparar() {
        insumo = new Insumo(EST, "GIN", "Gin", UnidadMedida.ML,
                new BigDecimal("750"), new BigDecimal("0.10"), new BigDecimal("100"), 750);
    }

    @Test
    void creacionDevuelve201UbicacionYDatosSinExponerVersionJpa() throws Exception {
        when(facade.crearInsumo(eq(EST), eq("GIN"), eq("Gin"), eq(UnidadMedida.ML), any(), any(), any(), eq(1))).thenReturn(insumo);
        mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(CREACION))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", RUTA + "/" + insumo.getId()))
                .andExpect(jsonPath("$.codigo").value("GIN"))
                .andExpect(jsonPath("$.stockActual").value(750))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.version").doesNotExist());
    }

    @Test
    void listaActivosPorDefectoYPermiteConsultarInactivos() throws Exception {
        when(facade.listarInsumos(EST, true)).thenReturn(List.of(insumo));
        when(facade.listarInsumos(EST, false)).thenReturn(List.of());
        mvc.perform(get(RUTA)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(insumo.getId().toString()));
        mvc.perform(get(RUTA + "?activo=false")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void consultaDetalleYStock() throws Exception {
        when(facade.consultarInsumo(EST, insumo.getId())).thenReturn(insumo);
        mvc.perform(get(RUTA + "/{id}", insumo.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Gin"));
        mvc.perform(get(RUTA + "/{id}/stock", insumo.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadMedida").value("ML"))
                .andExpect(jsonPath("$.stockActual").value(750));
    }

    @Test
    void editaDaDeBajaYReactivaMedianteFacade() throws Exception {
        when(facade.editarInsumo(eq(EST), eq(insumo.getId()), eq("Gin premium"), any(), any(), any())).thenReturn(insumo);
        when(facade.darDeAltaInsumo(EST, insumo.getId())).thenReturn(insumo);
        mvc.perform(put(RUTA + "/{id}", insumo.getId())
                .contentType(MediaType.APPLICATION_JSON).content(EDICION)).andExpect(status().isOk());
        verify(facade).editarInsumo(EST, insumo.getId(), "Gin premium", new BigDecimal("0.25"), new BigDecimal("150"), null);
        mvc.perform(delete(RUTA + "/{id}", insumo.getId())).andExpect(status().isNoContent());
        verify(facade).darDeBajaInsumo(EST, insumo.getId());
        mvc.perform(post(RUTA + "/{id}/alta", insumo.getId())).andExpect(status().isOk());
        verify(facade).darDeAltaInsumo(EST, insumo.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0.0001", "0.5", "750.0", "1000000000000", "null"})
    void rechazaStockInvalidoAntesDeInvocarElServicio(String stock) throws Exception {
        mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON)
                .content(CREACION.replace("750", stock)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.stockInicial").exists());
        verifyNoInteractions(facade);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.5", "150.0", "1000000000000"})
    void rechazaPuntoDeReordenNoEnteroOExcesivoEnCreacionYEdicion(String punto) throws Exception {
        mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON)
                .content(CREACION.replace("\"puntoReorden\":100", "\"puntoReorden\":" + punto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.puntoReorden").exists());
        mvc.perform(put(RUTA + "/{id}", insumo.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(EDICION.replace("\"puntoReorden\":150", "\"puntoReorden\":" + punto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.puntoReorden").exists());
        verifyNoInteractions(facade);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{", "null", "{\"unidadMedida\":\"LITRO\"}"})
    void rechazaJsonIncompletoInvalidoOUnidadDesconocida(String json) throws Exception {
        mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(facade);
    }

    @Test
    void validaNombreCostoYPuntoDeReordenEnEdicion() throws Exception {
        mvc.perform(put(RUTA + "/{id}", insumo.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":" ", "costoUnitario":-1, "puntoReorden":0.0001}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.costoUnitario").exists())
                .andExpect(jsonPath("$.errores.puntoReorden").exists());
        verifyNoInteractions(facade);
    }

    @Test
    void uuidOBooleanInvalidoDevuelve400() throws Exception {
        mvc.perform(get(RUTA + "/no-es-uuid")).andExpect(status().isBadRequest());
        mvc.perform(get(RUTA + "?activo=otro")).andExpect(status().isBadRequest());
        verifyNoInteractions(facade);
    }

    @Test
    void inexistentesDevuelven404() throws Exception {
        UUID id = UUID.randomUUID();
        when(facade.consultarInsumo(EST, id)).thenThrow(new InsumoNoEncontradoException(id));
        when(facade.editarInsumo(eq(EST), eq(id), any(), any(), any(), any())).thenThrow(new InsumoNoEncontradoException(id));
        when(facade.darDeAltaInsumo(EST, id)).thenThrow(new InsumoNoEncontradoException(id));
        doThrow(new InsumoNoEncontradoException(id)).when(facade).darDeBajaInsumo(EST, id);
        mvc.perform(get(RUTA + "/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(get(RUTA + "/{id}/stock", id)).andExpect(status().isNotFound());
        mvc.perform(put(RUTA + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(EDICION))
                .andExpect(status().isNotFound());
        mvc.perform(delete(RUTA + "/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(post(RUTA + "/{id}/alta", id)).andExpect(status().isNotFound());
    }

    @Test
    void duplicadosYConflictosDeBaseDevuelven409() throws Exception {
        when(facade.crearInsumo(any(), any(), any(), any(), any(), any(), any(), anyInt()))
                .thenThrow(new CodigoInsumoDuplicadoException())
                .thenThrow(new DataIntegrityViolationException("detalle SQL interno"));
        mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(CREACION))
                .andExpect(status().isConflict());
        mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(CREACION))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Los datos entran en conflicto con un registro existente o una restricción de integridad"));
    }

    @Test
    void edicionDeInactivoDevuelve409() throws Exception {
        when(facade.editarInsumo(any(), any(), any(), any(), any(), any())).thenThrow(new InsumoInactivoException());
        mvc.perform(put(RUTA + "/{id}", insumo.getId()).contentType(MediaType.APPLICATION_JSON).content(EDICION))
                .andExpect(status().isConflict());
    }
}
