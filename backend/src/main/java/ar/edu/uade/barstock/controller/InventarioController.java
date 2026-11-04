package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.dto.CrearInsumoRequest;
import ar.edu.uade.barstock.controller.dto.EditarInsumoRequest;
import ar.edu.uade.barstock.controller.dto.InsumoResponse;
import ar.edu.uade.barstock.controller.dto.StockInsumoResponse;
import ar.edu.uade.barstock.facade.BarStockFacade;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/establecimientos/{establecimientoId}/insumos")
public class InventarioController {
    private final BarStockFacade facade;

    public InventarioController(BarStockFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<InsumoResponse> crear(@PathVariable UUID establecimientoId,
                                                @Valid @RequestBody CrearInsumoRequest request) {
        var insumo = facade.crearInsumo(establecimientoId, request.codigo(), request.nombre(), request.unidadMedida(),
                request.stockInicial(), request.costoUnitario(), request.puntoReorden(),
                request.contenidoEnvaseOPorDefecto());
        return ResponseEntity.created(URI.create("/api/establecimientos/" + establecimientoId + "/insumos/" + insumo.getId()))
                .body(InsumoResponse.desde(insumo));
    }

    @GetMapping
    public List<InsumoResponse> listar(@PathVariable UUID establecimientoId,
                                       @RequestParam(defaultValue = "true") boolean activo) {
        return facade.listarInsumos(establecimientoId, activo).stream().map(InsumoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public InsumoResponse consultar(@PathVariable UUID establecimientoId, @PathVariable UUID id) {
        return InsumoResponse.desde(facade.consultarInsumo(establecimientoId, id));
    }

    @GetMapping("/{id}/stock")
    public StockInsumoResponse consultarStock(@PathVariable UUID establecimientoId, @PathVariable UUID id) {
        return StockInsumoResponse.desde(facade.consultarInsumo(establecimientoId, id));
    }

    @PutMapping("/{id}")
    public InsumoResponse editar(@PathVariable UUID establecimientoId, @PathVariable UUID id,
                                 @Valid @RequestBody EditarInsumoRequest request) {
        return InsumoResponse.desde(facade.editarInsumo(establecimientoId, id, request.nombre(),
                request.costoUnitario(), request.puntoReorden(), request.contenidoEnvase()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable UUID establecimientoId, @PathVariable UUID id) {
        facade.darDeBajaInsumo(establecimientoId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/alta")
    public InsumoResponse darDeAlta(@PathVariable UUID establecimientoId, @PathVariable UUID id) {
        return InsumoResponse.desde(facade.darDeAltaInsumo(establecimientoId, id));
    }
}
