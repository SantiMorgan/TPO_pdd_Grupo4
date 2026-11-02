package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.dto.AnularComandaRequest;
import ar.edu.uade.barstock.controller.dto.ComandaResponse;
import ar.edu.uade.barstock.controller.dto.RegistrarComandaRequest;
import ar.edu.uade.barstock.facade.BarStockFacade;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/establecimientos/{establecimientoId}/comandas")
public class ComandaController {
    private final BarStockFacade facade;

    public ComandaController(BarStockFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<ComandaResponse> registrar(@PathVariable UUID establecimientoId,
                                                     @Valid @RequestBody RegistrarComandaRequest request) {
        var comanda = facade.registrarComanda(establecimientoId, request.origen(), request.cliente(), request.pedido());
        return ResponseEntity.created(URI.create("/api/establecimientos/" + establecimientoId + "/comandas/" + comanda.getId()))
                .body(ComandaResponse.desde(comanda));
    }

    /** {@code enCurso=true}: registradas y en preparación, de la más antigua a la más nueva. Si no, las últimas. */
    @GetMapping
    public List<ComandaResponse> listar(@PathVariable UUID establecimientoId,
                                        @RequestParam(defaultValue = "false") boolean enCurso,
                                        @RequestParam(defaultValue = "30") int limite) {
        var comandas = enCurso ? facade.listarComandasEnCurso(establecimientoId)
                : facade.listarComandasRecientes(establecimientoId, limite);
        return comandas.stream().map(ComandaResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public ComandaResponse consultar(@PathVariable UUID establecimientoId, @PathVariable UUID id) {
        return ComandaResponse.desde(facade.consultarComanda(establecimientoId, id));
    }

    @PostMapping("/{id}/preparar")
    public ComandaResponse preparar(@PathVariable UUID establecimientoId, @PathVariable UUID id) {
        return ComandaResponse.desde(facade.prepararComanda(establecimientoId, id));
    }

    @PostMapping("/{id}/despachar")
    public ComandaResponse despachar(@PathVariable UUID establecimientoId, @PathVariable UUID id) {
        return ComandaResponse.desde(facade.despacharComanda(establecimientoId, id));
    }

    @PostMapping("/{id}/anular")
    public ComandaResponse anular(@PathVariable UUID establecimientoId, @PathVariable UUID id,
                                  @Valid @RequestBody AnularComandaRequest request) {
        return ComandaResponse.desde(facade.anularComanda(establecimientoId, id, request.motivo()));
    }
}
