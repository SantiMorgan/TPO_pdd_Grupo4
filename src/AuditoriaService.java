import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuditoriaService {

    public List<Merma> auditar(
            Inventario inventario,
            Map<String, Double> stockFisico) {

        List<Merma> resultado =
                new ArrayList<>();

        for (Insumo insumo :
                inventario.getInsumos().values()) {

            Double real =
                    stockFisico.get(
                            insumo.getCodigo()
                    );

            if (real != null) {

                resultado.add(
                        new Merma(
                                insumo.getNombre(),
                                insumo.getStockMl(),
                                real
                        )
                );
            }
        }

        return resultado;
    }
}