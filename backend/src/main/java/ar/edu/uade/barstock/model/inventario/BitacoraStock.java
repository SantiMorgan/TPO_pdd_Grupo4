package ar.edu.uade.barstock.model.inventario;

import ar.edu.uade.barstock.model.traza.TrazaPatrones;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Segundo observador: deja cada cambio de stock en la traza. Muestra la relación uno a muchos. */
@Component
@Order(1)
public class BitacoraStock implements ObservadorStock {
    private final TrazaPatrones traza;

    public BitacoraStock(TrazaPatrones traza) {
        this.traza = traza;
    }

    @Override
    public void actualizar(EventoStock evento) {
        String unidad = evento.unidad().simbolo();
        traza.registrar(evento.establecimientoId(), "Observer", "Inventario avisó: %s %s → %s %s".formatted(
                evento.nombreInsumo(), evento.stockAnterior().toPlainString(), evento.stockActual().toPlainString(), unidad));
    }
}
