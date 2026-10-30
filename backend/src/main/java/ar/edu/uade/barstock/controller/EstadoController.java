package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.dto.EstadoResponse;
import ar.edu.uade.barstock.facade.BarStockFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EstadoController {
    private final BarStockFacade facade;

    public EstadoController(BarStockFacade facade) {
        this.facade = facade;
    }

    @GetMapping({"", "/"})
    public EstadoResponse estado() {
        return new EstadoResponse(facade.obtenerNombreAplicacion(), "Hito 0");
    }
}
