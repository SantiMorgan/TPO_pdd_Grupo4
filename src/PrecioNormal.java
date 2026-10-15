public class PrecioNormal
        implements EstrategiaPrecio {

    @Override
    public double calcular(
            Receta receta,
            int cantidad) {

        return receta.getPrecioBase() * cantidad;
    }
}
