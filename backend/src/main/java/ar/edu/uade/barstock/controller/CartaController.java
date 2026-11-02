package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.dto.CartaResponse;
import ar.edu.uade.barstock.controller.dto.ComparacionPreciosResponse;
import ar.edu.uade.barstock.facade.BarStockFacade;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/establecimientos/{establecimientoId}")
public class CartaController {
    private final BarStockFacade facade;

    public CartaController(BarStockFacade facade) {
        this.facade = facade;
    }

    /** Menú con el precio de la estrategia vigente y la disponibilidad actual de cada trago. */
    @GetMapping("/carta")
    public CartaResponse carta(@PathVariable UUID establecimientoId) {
        return CartaResponse.desde(facade.consultarCarta(establecimientoId));
    }

    /** Cada trago con el precio que tendría bajo cada estrategia. */
    @GetMapping("/precios/comparacion")
    public ComparacionPreciosResponse comparacion(@PathVariable UUID establecimientoId) {
        return ComparacionPreciosResponse.desde(facade.compararEstrategias(establecimientoId));
    }
}
