public class IngredienteReceta {

    private Insumo insumo;
    private double cantidadMl;

    public IngredienteReceta(Insumo insumo, double cantidadMl) {
        this.insumo = insumo;
        this.cantidadMl = cantidadMl;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public double getCantidadMl() {
        return cantidadMl;
    }
}