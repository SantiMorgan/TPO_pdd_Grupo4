public class EstadoDespachada
        implements EstadoComanda {

    @Override
    public void avanzar(Comanda comanda) {

        throw new IllegalStateException("La comanda ya fue despachada.");
    }

    @Override
    public String getNombre() {
        return "DESPACHADA";
    }
}