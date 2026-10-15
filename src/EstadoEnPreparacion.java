public class EstadoEnPreparacion
        implements EstadoComanda {

    @Override
    public void avanzar(Comanda comanda) {

        comanda.setEstado(
                new EstadoDespachada()
        );
    }

    @Override
    public String getNombre() {
        return "EN PREPARACION";
    }
}
