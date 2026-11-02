package ar.edu.uade.barstock.model.comanda.state;

import java.util.List;

/** Los estados no guardan datos propios, así que existe una sola instancia de cada uno. */
public final class EstadosComanda {
    public static final EstadoComanda REGISTRADA = new Registrada();
    public static final EstadoComanda EN_PREPARACION = new EnPreparacion();
    public static final EstadoComanda DESPACHADA = new Despachada();
    public static final EstadoComanda ANULADA = new Anulada();

    private static final List<EstadoComanda> TODOS = List.of(REGISTRADA, EN_PREPARACION, DESPACHADA, ANULADA);

    private EstadosComanda() {
    }

    public static EstadoComanda desde(String codigo) {
        return TODOS.stream().filter(estado -> estado.codigo().equals(codigo)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de comanda desconocido: " + codigo));
    }

    public static List<EstadoComanda> enCurso() {
        return TODOS.stream().filter(EstadoComanda::enCurso).toList();
    }

    public static List<EstadoComanda> todos() {
        return TODOS;
    }
}
