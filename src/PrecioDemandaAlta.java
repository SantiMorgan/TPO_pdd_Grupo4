public class PrecioDemandaAlta
        implements EstrategiaPrecio {

    private double recargo;

    public PrecioDemandaAlta(double recargo) {
        this.recargo = recargo;
    }

    @Override
    public double calcular(
            Receta receta,
            int cantidad) {

        double precio =
                receta.getPrecioBase() * cantidad;

        return precio * (1 + recargo);
    }
}