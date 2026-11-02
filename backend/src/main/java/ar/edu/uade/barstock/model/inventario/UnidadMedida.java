package ar.edu.uade.barstock.model.inventario;

public enum UnidadMedida {
    ML("ml"), UNIDAD("u.");

    private final String simbolo;

    UnidadMedida(String simbolo) {
        this.simbolo = simbolo;
    }

    public String simbolo() {
        return simbolo;
    }
}
