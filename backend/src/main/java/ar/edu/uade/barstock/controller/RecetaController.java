package ar.edu.uade.barstock.controller;

import ar.edu.uade.barstock.controller.dto.RecetaRequest;
import ar.edu.uade.barstock.controller.dto.RecetaResponse;
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
@RequestMapping("/api/establecimientos/{establecimientoId}/recetas")
public class RecetaController {
    private final BarStockFacade facade;

    public RecetaController(BarStockFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public List<RecetaResponse> listar(@PathVariable UUID establecimientoId,
                                       @RequestParam(defaultValue = "false") boolean incluirFueraDelMenu) {
        return facade.listarRecetas(establecimientoId, incluirFueraDelMenu).stream().map(RecetaResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public RecetaResponse consultar(@PathVariable UUID establecimientoId, @PathVariable UUID id) {
        return RecetaResponse.desde(facade.consultarReceta(establecimientoId, id));
    }

    @PostMapping
    public ResponseEntity<RecetaResponse> crear(@PathVariable UUID establecimientoId,
                                                @Valid @RequestBody RecetaRequest request) {
        var receta = facade.crearReceta(establecimientoId, request.nombre(), request.descripcion(), request.icono(),
                request.precioBase(), request.cantidadesPorInsumo());
        return ResponseEntity.created(URI.create("/api/establecimientos/" + establecimientoId + "/recetas/" + receta.getId()))
                .body(RecetaResponse.desde(receta));
    }

    @PutMapping("/{id}")
    public RecetaResponse editar(@PathVariable UUID establecimientoId, @PathVariable UUID id,
                                 @Valid @RequestBody RecetaRequest request) {
        return RecetaResponse.desde(facade.editarReceta(establecimientoId, id, request.nombre(), request.descripcion(),
                request.icono(), request.precioBase(), request.cantidadesPorInsumo()));
    }

    /** Baja lógica: la receta sale del menú pero las comandas anteriores la siguen referenciando. */
    @DeleteMapping("/{id}")
    public RecetaResponse quitarDelMenu(@PathVariable UUID establecimientoId, @PathVariable UUID id) {
        return RecetaResponse.desde(facade.quitarRecetaDelMenu(establecimientoId, id));
    }

    @PostMapping("/{id}/alta")
    public RecetaResponse volverAlMenu(@PathVariable UUID establecimientoId, @PathVariable UUID id) {
        return RecetaResponse.desde(facade.volverRecetaAlMenu(establecimientoId, id));
    }
}
