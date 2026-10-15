import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarStockFacade {

    private Inventario inventario;
    private CalculadoraPrecio calculadoraPrecio;

    private Map<String, Receta> recetas;
    private Map<Integer, Comanda> comandas;

    private List<Venta> ventas;

    private int siguienteComanda = 1;

    public BarStockFacade(Inventario inventario) {

        this.inventario = inventario;

        this.calculadoraPrecio =
                new CalculadoraPrecio(
                        new PrecioNormal()
                );

        recetas = new HashMap<>();
        comandas = new HashMap<>();
        ventas = new ArrayList<>();
    }

    public void registrarReceta(Receta receta) {

        recetas.put(
                receta.getNombre(),
                receta
        );
    }

    public void cambiarEstrategiaPrecio(
            EstrategiaPrecio estrategia) {

        calculadoraPrecio.setEstrategia(
                estrategia
        );
    }

    public int crearComanda(
            String receta,
            int cantidad) {

        Receta recetaEncontrada =
                recetas.get(receta);

        if (recetaEncontrada == null) {

            throw new IllegalArgumentException(
                    "La receta no existe"
            );
        }

        int numero = siguienteComanda++;

        Comanda comanda =
                new Comanda(
                        numero,
                        recetaEncontrada,
                        cantidad
                );

        comandas.put(
                numero,
                comanda
        );

        return numero;
    }

    public void iniciarPreparacion(
            int numeroComanda) {

        Comanda comanda =
                obtenerComanda(numeroComanda);

        comanda.avanzarEstado();
    }

    public double despachar(
            int numeroComanda) {

        Comanda comanda =
                obtenerComanda(numeroComanda);

        if (!comanda.getEstado()
                .equals("EN PREPARACION")) {

            throw new IllegalStateException(
                    "La comanda debe estar en preparación"
            );
        }

        inventario.consumirReceta(
                comanda.getReceta(),
                comanda.getCantidad()
        );

        double total =
                calculadoraPrecio.calcular(
                        comanda.getReceta(),
                        comanda.getCantidad()
                );

        Venta venta =
                new Venta(
                        comanda.getReceta(),
                        comanda.getCantidad(),
                        total
                );

        ventas.add(venta);

        comanda.avanzarEstado();

        return total;
    }

    private Comanda obtenerComanda(
            int numero) {

        Comanda comanda =
                comandas.get(numero);

        if (comanda == null) {

            throw new IllegalArgumentException(
                    "Comanda inexistente"
            );
        }

        return comanda;
    }

    public List<Venta> getVentas() {
        return ventas;
    }

    public Inventario getInventario() {
        return inventario;
    }
}