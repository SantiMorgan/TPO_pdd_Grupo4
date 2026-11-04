package ar.edu.uade.barstock.model.traza;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Registro en memoria de lo que hace cada patrón, para mostrarlo en la interfaz durante la defensa.
 * Fabricación pura (GRASP): no existe en el dominio del bar. Dentro de una transacción los eventos
 * se publican recién al confirmarla; si se revierte, se descartan, igual que los datos.
 */
@Component
public class TrazaPatrones {
    private static final Logger log = LoggerFactory.getLogger(TrazaPatrones.class);
    private static final int MAXIMO_POR_ESTABLECIMIENTO = 80;

    private final Map<UUID, Deque<EventoPatron>> eventos = new ConcurrentHashMap<>();
    private final ThreadLocal<List<Pendiente>> pendientes = new ThreadLocal<>();
    private final Clock reloj;

    public TrazaPatrones(Clock reloj) {
        this.reloj = reloj;
    }

    public void registrar(UUID establecimientoId, String patron, String descripcion) {
        Pendiente pendiente = new Pendiente(establecimientoId, new EventoPatron(Instant.now(reloj), patron, descripcion));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publicar(pendiente);
            return;
        }
        List<Pendiente> lista = pendientes.get();
        if (lista == null) {
            lista = new ArrayList<>();
            pendientes.set(lista);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int estado) {
                    List<Pendiente> registrados = pendientes.get();
                    pendientes.remove();
                    if (estado == STATUS_COMMITTED && registrados != null) {
                        registrados.forEach(TrazaPatrones.this::publicar);
                    }
                }
            });
        }
        lista.add(pendiente);
    }

    /** Del más reciente al más antiguo. */
    public List<EventoPatron> consultar(UUID establecimientoId) {
        return List.copyOf(eventos.getOrDefault(establecimientoId, new ConcurrentLinkedDeque<>()));
    }

    private void publicar(Pendiente pendiente) {
        EventoPatron evento = pendiente.evento();
        log.info("[{}] {}", evento.patron(), evento.descripcion());
        Deque<EventoPatron> deque = eventos.computeIfAbsent(pendiente.establecimientoId(), id -> new ConcurrentLinkedDeque<>());
        deque.addFirst(evento);
        while (deque.size() > MAXIMO_POR_ESTABLECIMIENTO) {
            deque.pollLast();
        }
    }

    private record Pendiente(UUID establecimientoId, EventoPatron evento) {
    }
}
