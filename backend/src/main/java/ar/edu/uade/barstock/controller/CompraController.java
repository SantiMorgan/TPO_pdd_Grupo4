package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.dto.CompraRequest;
import ar.edu.uade.barstock.controller.dto.MovimientoResponse;
import ar.edu.uade.barstock.facade.BarStockFacade;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/establecimientos/{establecimientoId}")
public class CompraController {
    private final BarStockFacade facade;

    public CompraController(BarStockFacade facade) {
        this.facade = facade;
    }

    @PostMapping("/compras")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse registrar(@PathVariable UUID establecimientoId, @Valid @RequestBody CompraRequest request) {
        return MovimientoResponse.desde(facade.registrarCompra(establecimientoId, request.insumoId(), request.envases()));
    }

    @GetMapping("/movimientos")
    public List<MovimientoResponse> movimientos(@PathVariable UUID establecimientoId,
                                                @RequestParam(defaultValue = "20") int limite) {
        return facade.listarMovimientos(establecimientoId, limite).stream().map(MovimientoResponse::desde).toList();
    }
}
