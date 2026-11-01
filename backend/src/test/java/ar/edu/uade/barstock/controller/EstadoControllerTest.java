package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.facade.BarStockFacade;
import ar.edu.uade.barstock.model.inventario.InventarioService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;

class EstadoControllerTest {
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new EstadoController(new BarStockFacade(mock(InventarioService.class))))
            .build();

    @Test
    void publicaElEstadoInicialEnAmbasRutas() throws Exception {
        for (String ruta : new String[]{"/api", "/api/"}) {
            mvc.perform(get(ruta))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.aplicacion").value("BarStock Solutions"))
                    .andExpect(jsonPath("$.hito").value("Hito 0"));
        }
    }
}
