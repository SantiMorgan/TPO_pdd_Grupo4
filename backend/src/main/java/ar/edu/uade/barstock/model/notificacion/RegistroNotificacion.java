package ar.edu.uade.barstock.model.notificacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notificacion", schema = "barstock")
public class RegistroNotificacion {
    @Id
    private UUID id;

    @Column(name = "establecimiento_id", nullable = false)
    private UUID establecimientoId;

    @Column(name = "insumo_id")
    private UUID insumoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoNotificacion canal;

    @Column(nullable = false, length = 120)
    private String destinatario;

    @Column(nullable = false, length = 300)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoNotificacion resultado;

    @Column(nullable = false)
    private Instant fecha;

    protected RegistroNotificacion() {
        // Constructor requerido por JPA.
    }

    public RegistroNotificacion(MensajeAlerta mensaje, TipoNotificacion canal, ResultadoNotificacion resultado,
                                Instant fecha) {
        this.id = UUID.randomUUID();
        this.establecimientoId = mensaje.establecimientoId();
        this.insumoId = mensaje.insumoId();
        this.canal = canal;
        this.destinatario = recortar(mensaje.destinatario() == null ? "(sin destinatario)" : mensaje.destinatario(), 120);
        this.mensaje = recortar(mensaje.texto(), 300);
        this.resultado = resultado;
        this.fecha = fecha;
    }

    private static String recortar(String texto, int maximo) {
        return texto.length() > maximo ? texto.substring(0, maximo) : texto;
    }

    public UUID getId() { return id; }
    public UUID getEstablecimientoId() { return establecimientoId; }
    public UUID getInsumoId() { return insumoId; }
    public TipoNotificacion getCanal() { return canal; }
    public String getDestinatario() { return destinatario; }
    public String getMensaje() { return mensaje; }
    public ResultadoNotificacion getResultado() { return resultado; }
    public Instant getFecha() { return fecha; }
}
