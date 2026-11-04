package ar.edu.uade.barstock.model.inventario;

/** Observer: cualquier interesado en los cambios de stock, sin que el Inventario sepa quién es. */
public interface ObservadorStock {
    void actualizar(EventoStock evento);
}
