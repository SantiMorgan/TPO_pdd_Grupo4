public class Comanda {

    private int numero;
    private Receta receta;
    private int cantidad;

    private EstadoComanda estado;

    public Comanda(
            int numero,
            Receta receta,
            int cantidad) {

        this.numero = numero;
        this.receta = receta;
        this.cantidad = cantidad;

        this.estado =
                new EstadoRegistrada();
    }

    public void avanzarEstado() {
        estado.avanzar(this);
    }

    public void setEstado(
            EstadoComanda estado) {

        this.estado = estado;
    }

    public String getEstado() {
        return estado.getNombre();
    }

    public int getNumero() {
        return numero;
    }

    public Receta getReceta() {
        return receta;
    }

    public int getCantidad() {
        return cantidad;
    }
}