package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.error.InventarioExceptionHandler;
import ar.edu.uade.barstock.facade.BarStockFacade;
import ar.edu.uade.barstock.model.inventario.CodigoInsumoDuplicadoException;
import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.inventario.InsumoInactivoException;
import ar.edu.uade.barstock.model.inventario.InsumoNoEncontradoException;
import ar.edu.uade.barstock.model.inventario.InventarioService;
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
    private final InventarioService servicio = mock(InventarioService.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new InventarioController(new BarStockFacade(servicio)))
            .setControllerAdvice(new InventarioExceptionHandler()).build();
    private Insumo insumo;

    @BeforeEach
    void preparar() {
        insumo = new Insumo("GIN", "Gin", UnidadMedida.ML,
                new BigDecimal("750"), new BigDecimal("0.10"), new BigDecimal("100"));
    }

    @Test
    void creacionDevuelve201UbicacionYDatosSinExponerVersionJpa() throws Exception {
        when(servicio.crear(eq("GIN"), eq("Gin"), eq(UnidadMedida.ML), any(), any(), any())).thenReturn(insumo);
        mvc.perform(post("/api/insumos").contentType(MediaType.APPLICATION_JSON).content(CREACION))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/insumos/" + insumo.getId()))
                .andExpect(jsonPath("$.codigo").value("GIN"))
                .andExpect(jsonPath("$.stockActual").value(750))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.version").doesNotExist());
    }

    @Test
    void listaActivosPorDefectoYPermiteConsultarInactivos() throws Exception {
        when(servicio.listar(true)).thenReturn(List.of(insumo));
        when(servicio.listar(false)).thenReturn(List.of());
        mvc.perform(get("/api/insumos")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(insumo.getId().toString()));
        mvc.perform(get("/api/insumos?activo=false")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void consultaDetalleYStock() throws Exception {
        when(servicio.consultar(insumo.getId())).thenReturn(insumo);
        mvc.perform(get("/api/insumos/{id}", insumo.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Gin"));
        mvc.perform(get("/api/insumos/{id}/stock", insumo.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadMedida").value("ML"))
                .andExpect(jsonPath("$.stockActual").value(750));
    }

    @Test
    void editaDaDeBajaYReactivaMedianteFacade() throws Exception {
        when(servicio.editar(eq(insumo.getId()), eq("Gin premium"), any(), any())).thenReturn(insumo);
        when(servicio.darDeAlta(insumo.getId())).thenReturn(insumo);
        mvc.perform(put("/api/insumos/{id}", insumo.getId())
                .contentType(MediaType.APPLICATION_JSON).content(EDICION)).andExpect(status().isOk());
        verify(servicio).editar(insumo.getId(), "Gin premium", new BigDecimal("0.25"), new BigDecimal("150"));
        mvc.perform(delete("/api/insumos/{id}", insumo.getId())).andExpect(status().isNoContent());
        verify(servicio).darDeBaja(insumo.getId());
        mvc.perform(post("/api/insumos/{id}/alta", insumo.getId())).andExpect(status().isOk());
        verify(servicio).darDeAlta(insumo.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0.0001", "0.5", "750.0", "1000000000000", "null"})
    void rechazaStockInvalidoAntesDeInvocarElServicio(String stock) throws Exception {
        mvc.perform(post("/api/insumos").contentType(MediaType.APPLICATION_JSON)
                .content(CREACION.replace("750", stock)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.stockInicial").exists());
        verifyNoInteractions(servicio);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.5", "150.0", "1000000000000"})
    void rechazaPuntoDeReordenNoEnteroOExcesivoEnCreacionYEdicion(String punto) throws Exception {
        mvc.perform(post("/api/insumos").contentType(MediaType.APPLICATION_JSON)
                .content(CREACION.replace("\"puntoReorden\":100", "\"puntoReorden\":" + punto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.puntoReorden").exists());
        mvc.perform(put("/api/insumos/{id}", insumo.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(EDICION.replace("\"puntoReorden\":150", "\"puntoReorden\":" + punto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.puntoReorden").exists());
        verifyNoInteractions(servicio);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{", "null", "{\"unidadMedida\":\"LITRO\"}"})
    void rechazaJsonIncompletoInvalidoOUnidadDesconocida(String json) throws Exception {
        mvc.perform(post("/api/insumos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(servicio);
    }

    @Test
    void validaNombreCostoYPuntoDeReordenEnEdicion() throws Exception {
        mvc.perform(put("/api/insumos/{id}", insumo.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":" ", "costoUnitario":-1, "puntoReorden":0.0001}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.costoUnitario").exists())
                .andExpect(jsonPath("$.errores.puntoReorden").exists());
        verifyNoInteractions(servicio);
    }

    @Test
    void uuidOBooleanInvalidoDevuelve400() throws Exception {
        mvc.perform(get("/api/insumos/no-es-uuid")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/insumos?activo=otro")).andExpect(status().isBadRequest());
        verifyNoInteractions(servicio);
    }

    @Test
    void inexistentesDevuelven404() throws Exception {
        UUID id = UUID.randomUUID();
        when(servicio.consultar(id)).thenThrow(new InsumoNoEncontradoException(id));
        when(servicio.editar(eq(id), any(), any(), any())).thenThrow(new InsumoNoEncontradoException(id));
        when(servicio.darDeAlta(id)).thenThrow(new InsumoNoEncontradoException(id));
        doThrow(new InsumoNoEncontradoException(id)).when(servicio).darDeBaja(id);
        mvc.perform(get("/api/insumos/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/insumos/{id}/stock", id)).andExpect(status().isNotFound());
        mvc.perform(put("/api/insumos/{id}", id).contentType(MediaType.APPLICATION_JSON).content(EDICION))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/insumos/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(post("/api/insumos/{id}/alta", id)).andExpect(status().isNotFound());
    }

    @Test
    void duplicadosYConflictosDeBaseDevuelven409() throws Exception {
        when(servicio.crear(any(), any(), any(), any(), any(), any()))
                .thenThrow(new CodigoInsumoDuplicadoException())
                .thenThrow(new DataIntegrityViolationException("detalle SQL interno"));
        mvc.perform(post("/api/insumos").contentType(MediaType.APPLICATION_JSON).content(CREACION))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/insumos").contentType(MediaType.APPLICATION_JSON).content(CREACION))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Los datos entran en conflicto con un registro existente o una restricción de integridad"));
    }

    @Test
    void edicionDeInactivoDevuelve409() throws Exception {
        when(servicio.editar(any(), any(), any(), any())).thenThrow(new InsumoInactivoException());
        mvc.perform(put("/api/insumos/{id}", insumo.getId()).contentType(MediaType.APPLICATION_JSON).content(EDICION))
                .andExpect(status().isConflict());
    }
}
