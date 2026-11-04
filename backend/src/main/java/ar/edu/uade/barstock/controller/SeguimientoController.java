package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.dto.EventoPatronResponse;
import ar.edu.uade.barstock.controller.dto.NotificacionResponse;
import ar.edu.uade.barstock.facade.BarStockFacade;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Alertas emitidas por el Observer y traza de patrones para la demostración. */
@RestController
@RequestMapping("/api/establecimientos/{establecimientoId}")
public class SeguimientoController {
    private final BarStockFacade facade;

    public SeguimientoController(BarStockFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/notificaciones")
    public List<NotificacionResponse> notificaciones(@PathVariable UUID establecimientoId,
                                                     @RequestParam(defaultValue = "20") int limite) {
        return facade.listarNotificaciones(establecimientoId, limite).stream().map(NotificacionResponse::desde).toList();
    }

    @GetMapping("/traza")
    public List<EventoPatronResponse> traza(@PathVariable UUID establecimientoId) {
        return facade.consultarTraza(establecimientoId).stream().map(EventoPatronResponse::desde).toList();
    }
}
