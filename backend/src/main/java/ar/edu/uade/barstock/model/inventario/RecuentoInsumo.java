package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.Validacion;
import java.math.BigDecimal;

/**
 * Lo que se contó de un insumo: envases cerrados más lo que queda en el abierto
 * (por ejemplo, 2 botellas y 300 ml), y un comentario que justifica la diferencia.
 */
public record RecuentoInsumo(int envasesCompletos, BigDecimal restante, String comentario) {
    public RecuentoInsumo {
        if (envasesCompletos < 0) {
            throw new IllegalArgumentException("Los envases completos no pueden ser negativos");
        }
        if (restante == null || restante.signum() < 0) {
            throw new IllegalArgumentException("Lo que queda en el envase abierto no puede ser negativo");
        }
        comentario = Validacion.textoOpcional(comentario, 200, "comentario");
    }
}
