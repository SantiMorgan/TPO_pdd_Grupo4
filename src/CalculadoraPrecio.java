public class CalculadoraPrecio {

    private EstrategiaPrecio estrategia;

    public CalculadoraPrecio(
            EstrategiaPrecio estrategia) {

        this.estrategia = estrategia;
    }

    public void setEstrategia(EstrategiaPrecio estrategia) {

        this.estrategia = estrategia;
    }

    public double calcular(Receta receta, int cantidad) {

        return estrategia.calcular(receta, cantidad);
    }
}