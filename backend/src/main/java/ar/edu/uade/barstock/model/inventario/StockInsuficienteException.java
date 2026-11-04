package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.exception.ReglaDeNegocioException;
import java.util.List;
import java.util.stream.Collectors;

/** Se lanza antes de modificar cualquier insumo: el descuento es todo o nada. */
public class StockInsuficienteException extends ReglaDeNegocioException {
    private final List<Faltante> faltantes;

    public StockInsuficienteException(List<Faltante> faltantes) {
        super("Stock insuficiente. No se descontó ningún insumo: "
                + faltantes.stream().map(Faltante::describir).collect(Collectors.joining(" · ")));
        this.faltantes = List.copyOf(faltantes);
    }

    public List<Faltante> getFaltantes() {
        return faltantes;
    }
}
