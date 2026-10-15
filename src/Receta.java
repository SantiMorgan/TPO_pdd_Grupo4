import java.util.ArrayList;
import java.util.List;

public class Receta {

    //Este comportamiento implementa uno de las características principales de
    //BarStock: cada cóctel está asociado a una receta que determina cuántos mililitros deben descontarse de cada botella.

    private String nombre;
    private double precioBase;

    private List<IngredienteReceta> ingredientes;

    public Receta(String nombre, double precioBase) {
        this.nombre = nombre;
        this.precioBase = precioBase;
        ingredientes = new ArrayList<>();
    }

    public void agregarIngrediente(
            Insumo insumo,
            double cantidadMl) {

        ingredientes.add(
                new IngredienteReceta(insumo, cantidadMl)
        );
    }

    public double calcularCosto() {

        double costo = 0;

        for (IngredienteReceta ingrediente : ingredientes) {

            costo += ingrediente.getCantidadMl()
                    * ingrediente.getInsumo().getCostoPorMl();
        }

        return costo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public List<IngredienteReceta> getIngredientes() {
        return ingredientes;
    }
}