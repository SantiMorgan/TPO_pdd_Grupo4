import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inventario {

    private Map<String, Insumo> insumos;
    private List<StockObserver> observers;

    public Inventario() {
        insumos = new HashMap<>();
        observers = new ArrayList<>();
    }

    public void agregarInsumo(Insumo insumo) {
        insumos.put(insumo.getCodigo(), insumo);
    }

    public void agregarObserver(StockObserver observer) {
        observers.add(observer);
    }

    public void eliminarObserver(StockObserver observer) {
        observers.remove(observer);
    }

    private void notificar(Insumo insumo) {

        for (StockObserver observer : observers) {
            observer.actualizar(insumo);
        }
    }

    public void consumirReceta(Receta receta, int cantidad) {

        // Primero verificamos que exista stock
        for (IngredienteReceta ingrediente : receta.getIngredientes()) {

            double necesario = ingrediente.getCantidadMl() * cantidad;

            if (ingrediente.getInsumo().getStockMl()
                    < necesario) {

                throw new IllegalArgumentException("Stock insuficiente de " + ingrediente.getInsumo().getNombre());
            }
        }

        // Una vez validado, descontamos
        for (IngredienteReceta ingrediente : receta.getIngredientes()) {

            Insumo insumo = ingrediente.getInsumo();

            double cantidadADescontar = ingrediente.getCantidadMl() * cantidad;

            insumo.descontar(cantidadADescontar);

            notificar(insumo);
        }
    }

    public void reponer(String codigo, double cantidad) {

        Insumo insumo = insumos.get(codigo);

        if (insumo == null) {
            throw new IllegalArgumentException("Insumo inexistente");
        }

        insumo.reponer(cantidad);
    }

    public Map<String, Insumo> getInsumos() {
        return insumos;
    }
}