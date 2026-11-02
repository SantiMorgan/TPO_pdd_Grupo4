package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.facade.BarStockFacade;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EstadoControllerTest {
    private final BarStockFacade facade = mock(BarStockFacade.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new EstadoController(facade))
            .build();

    @Test
    void publicaElEstadoEnAmbasRutas() throws Exception {
        for (String ruta : new String[]{"/api", "/api/"}) {
            when(facade.obtenerNombreAplicacion()).thenReturn("BarStock Solutions");
            mvc.perform(get(ruta))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.aplicacion").value("BarStock Solutions"))
                    .andExpect(jsonPath("$.hito").value(EstadoController.HITO));
        }
    }
}
