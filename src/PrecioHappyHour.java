public class PrecioHappyHour
        implements EstrategiaPrecio {

    private double descuento;

    public PrecioHappyHour(double descuento) {
        this.descuento = descuento;
    }

    @Override
    public double calcular(
            Receta receta,
            int cantidad) {

        double precio =
                receta.getPrecioBase() * cantidad;

        return precio * (1 - descuento);
    }
}