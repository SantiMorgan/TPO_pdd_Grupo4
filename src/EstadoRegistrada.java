public class EstadoRegistrada
        implements EstadoComanda {

    @Override
    public void avanzar(Comanda comanda) {

        comanda.setEstado(new EstadoEnPreparacion());
    }

    @Override
    public String getNombre() {
        return "REGISTRADA";
    }
}
