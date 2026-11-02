package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.dto.AuditoriaResponse;
import ar.edu.uade.barstock.controller.dto.RecuentoRequest;
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
@RequestMapping("/api/establecimientos/{establecimientoId}/auditorias")
public class AuditoriaController {
    private final BarStockFacade facade;

    public AuditoriaController(BarStockFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuditoriaResponse registrar(@PathVariable UUID establecimientoId, @Valid @RequestBody RecuentoRequest request) {
        return AuditoriaResponse.desde(facade.auditarInventario(establecimientoId, request.responsable(), request.recuento()));
    }

    @GetMapping
    public List<AuditoriaResponse> listar(@PathVariable UUID establecimientoId,
                                          @RequestParam(defaultValue = "20") int limite) {
        return facade.listarAuditorias(establecimientoId, limite).stream().map(AuditoriaResponse::desde).toList();
    }
}
