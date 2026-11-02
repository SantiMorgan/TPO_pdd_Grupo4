package ar.edu.uade.barstock.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Recuento físico de cierre de turno: solo los insumos contados. */
public record RecuentoRequest(
        @NotBlank @Size(max = 60) String responsable,
        @NotEmpty @Size(max = 200) List<@Valid @NotNull Conteo> conteos) {

    public record Conteo(@NotNull UUID insumoId,
                         @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 0) BigDecimal stockFisico) {
    }

    public Map<UUID, BigDecimal> recuento() {
        Map<UUID, BigDecimal> recuento = new LinkedHashMap<>();
        for (Conteo conteo : conteos) {
            if (recuento.put(conteo.insumoId(), conteo.stockFisico()) != null) {
                throw new IllegalArgumentException("Cada insumo se cuenta una sola vez por cierre");
            }
        }
        return recuento;
    }
}
