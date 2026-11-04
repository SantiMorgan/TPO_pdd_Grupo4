package ar.edu.uade.barstock.model.precio;

import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.establecimiento.PoliticaPrecio;
import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Decide qué estrategia corresponde: la que fije el dueño o, en modo automático,
 * Happy Hour dentro del horario y, si no, precio normal.
 * Las comandas y la carta solo conocen la interfaz EstrategiaPrecio.
 */
@Component
public class SelectorEstrategiaPrecio {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final Clock reloj;

    public SelectorEstrategiaPrecio(Clock reloj) {
        this.reloj = reloj;
    }

    public EstrategiaVigente vigente(Establecimiento establecimiento) {
        PoliticaPrecio politica = establecimiento.getPoliticaPrecio();
        LocalTime hora = politica.horaDeReferencia(LocalTime.now(reloj).truncatedTo(ChronoUnit.MINUTES));
        boolean simulada = politica.getHoraSimulada() != null;
        return switch (politica.getModo()) {
            case NORMAL -> new EstrategiaVigente(new PrecioNormal(), "Fijado por el dueño", hora, simulada);
            case HAPPY_HOUR -> new EstrategiaVigente(happyHour(politica), "Fijado por el dueño", hora, simulada);
            case AUTOMATICO -> automatica(politica, hora, simulada);
        };
    }

    /** Las estrategias con los parámetros del establecimiento, para comparar precios. */
    public List<EstrategiaPrecio> todas(Establecimiento establecimiento) {
        return List.of(new PrecioNormal(), happyHour(establecimiento.getPoliticaPrecio()));
    }

    private EstrategiaVigente automatica(PoliticaPrecio politica, LocalTime hora, boolean simulada) {
        if (politica.esHappyHour(hora)) {
            return new EstrategiaVigente(happyHour(politica), "Horario de Happy Hour "
                    + HORA.format(politica.getInicioHappyHour()) + "–" + HORA.format(politica.getFinHappyHour()), hora, simulada);
        }
        return new EstrategiaVigente(new PrecioNormal(), "Horario normal", hora, simulada);
    }

    private static PrecioHappyHour happyHour(PoliticaPrecio politica) {
        return new PrecioHappyHour(politica.getDescuentoHappyHour(), politica.getMargenMinimo());
    }
}
