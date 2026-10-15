public class Insumo {
//Esta clase representa los insumos que en el proyecto
//tienen código único, unidad de medida, costo y punto de reorden.

    private String codigo;
    private String nombre;
    private double stockMl;
    private double puntoReordenMl;
    private double costoPorMl;

    public Insumo(String codigo, String nombre, double stockMl, double puntoReordenMl, double costoPorMl) {

        this.codigo = codigo;
        this.nombre = nombre;
        this.stockMl = stockMl;
        this.puntoReordenMl = puntoReordenMl;
        this.costoPorMl = costoPorMl;
    }

    public void descontar(double cantidadMl) {

        if (cantidadMl > stockMl) {
            throw new IllegalArgumentException("Stock insuficiente de " + nombre);
        }

        stockMl -= cantidadMl;
    }

    public void reponer(double cantidadMl) {
        stockMl += cantidadMl;
    }

    public boolean necesitaReposicion() {
        return stockMl <= puntoReordenMl;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getStockMl() {
        return stockMl;
    }

    public double getPuntoReordenMl() {
        return puntoReordenMl;
    }

    public double getCostoPorMl() {
        return costoPorMl;
    }
}
