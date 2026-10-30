package ar.edu.uade.barstock.facade;

import org.springframework.stereotype.Service;

/** Punto de entrada para incorporar los casos de uso del dominio en los siguientes hitos. */
@Service
public class BarStockFacade {
    public String obtenerNombreAplicacion() {
        return "BarStock Solutions";
    }
}
