package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.dto.AlertasDto;
import ar.edu.uade.barstock.controller.dto.EstablecimientoRequest;
import ar.edu.uade.barstock.controller.dto.EstablecimientoResponse;
import ar.edu.uade.barstock.controller.dto.PoliticaPrecioDto;
import ar.edu.uade.barstock.facade.BarStockFacade;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/establecimientos")
public class EstablecimientoController {
    private final BarStockFacade facade;

    public EstablecimientoController(BarStockFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public List<EstablecimientoResponse> listar() {
        return facade.listarEstablecimientos().stream().map(EstablecimientoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public EstablecimientoResponse consultar(@PathVariable UUID id) {
        return EstablecimientoResponse.desde(facade.consultarEstablecimiento(id));
    }

    @PostMapping
    public ResponseEntity<EstablecimientoResponse> crear(@Valid @RequestBody EstablecimientoRequest request) {
        var establecimiento = facade.crearEstablecimiento(request.codigo(), request.nombre(), request.zona(),
                request.descripcion());
        return ResponseEntity.created(URI.create("/api/establecimientos/" + establecimiento.getId()))
                .body(EstablecimientoResponse.desde(establecimiento));
    }

    @PutMapping("/{id}")
    public EstablecimientoResponse editar(@PathVariable UUID id, @Valid @RequestBody EstablecimientoRequest request) {
        return EstablecimientoResponse.desde(facade.editarEstablecimiento(id, request.nombre(), request.zona(),
                request.descripcion()));
    }

    @PutMapping("/{id}/politica-precio")
    public EstablecimientoResponse configurarPrecios(@PathVariable UUID id, @Valid @RequestBody PoliticaPrecioDto request) {
        return EstablecimientoResponse.desde(facade.configurarPrecios(id, request.aModelo()));
    }

    @PutMapping("/{id}/alertas")
    public EstablecimientoResponse configurarAlertas(@PathVariable UUID id, @Valid @RequestBody AlertasDto request) {
        return EstablecimientoResponse.desde(facade.configurarAlertas(id, request.aModelo()));
    }
}
