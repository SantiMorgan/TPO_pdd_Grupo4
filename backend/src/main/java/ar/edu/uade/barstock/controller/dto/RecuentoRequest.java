package ar.edu.uade.barstock.controller.dto;

import ar.edu.uade.barstock.model.inventario.RecuentoInsumo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Recuento físico de cierre de turno: solo los insumos contados. Cada uno se cuenta en envases
 * cerrados más lo que queda en el abierto, con un comentario opcional que justifica la diferencia.
 */
public record RecuentoRequest(
        @NotBlank @Size(max = 60) String responsable,
        @NotEmpty @Size(max = 200) List<@Valid @NotNull Conteo> conteos) {

    public record Conteo(@NotNull UUID insumoId,
                         @NotNull @Min(0) @Max(99999) Integer envasesCompletos,
                         @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 0) BigDecimal restante,
                         @Size(max = 200) String comentario) {
    }

    public Map<UUID, RecuentoInsumo> recuento() {
        Map<UUID, RecuentoInsumo> recuento = new LinkedHashMap<>();
        for (Conteo conteo : conteos) {
            RecuentoInsumo contado = new RecuentoInsumo(conteo.envasesCompletos(), conteo.restante(), conteo.comentario());
            if (recuento.put(conteo.insumoId(), contado) != null) {
                throw new IllegalArgumentException("Cada insumo se cuenta una sola vez por cierre");
            }
        }
        return recuento;
    }
}
