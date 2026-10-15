public class Venta {
    //Esta clase representa una venta realizada. Guarda la Receta, la cantidad vendida y el total cobrado.
    // Además, calcularCosto() obtiene el costo total de los tragos vendidos
    // y calcularGanancia() calcula la diferencia entre lo cobrado y el costo.

    private Receta receta;
    private int cantidad;
    private double total;

    public Venta(
            Receta receta,
            int cantidad,
            double total) {

        this.receta = receta;
        this.cantidad = cantidad;
        this.total = total;
    }

    public double calcularCosto() {
        return receta.calcularCosto() * cantidad;
    }

    public double calcularGanancia() {
        return total - calcularCosto();
    }

    public Receta getReceta() {
        return receta;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getTotal() {
        return total;
    }
}