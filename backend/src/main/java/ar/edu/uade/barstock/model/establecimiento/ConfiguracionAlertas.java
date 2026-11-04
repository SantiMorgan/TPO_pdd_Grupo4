package ar.edu.uade.barstock.model.establecimiento;

import ar.edu.uade.barstock.model.Validacion;
import ar.edu.uade.barstock.model.notificacion.TipoNotificacion;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Canales por los que el establecimiento quiere recibir las alertas de reposición y sus contactos. */
@Embeddable
public class ConfiguracionAlertas {
    @Column(name = "canales_alerta", nullable = false, length = 60)
    private String canales;

    @Column(name = "email_alertas", length = 120)
    private String email;

    @Column(name = "telefono_alertas", length = 30)
    private String telefono;

    protected ConfiguracionAlertas() {
        // Constructor requerido por JPA.
    }

    public ConfiguracionAlertas(Collection<TipoNotificacion> canales, String email, String telefono) {
        // Se recorre en lugar de usar contains(null): las colecciones inmutables lo rechazan con NPE.
        if (canales == null || canales.isEmpty() || canales.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Elegí al menos un canal de alerta");
        }
        Set<TipoNotificacion> elegidos = EnumSet.copyOf(canales);
        String emailValidado = Validacion.textoOpcional(email, 120, "email");
        String telefonoValidado = Validacion.textoOpcional(telefono, 30, "telefono");
        if (elegidos.contains(TipoNotificacion.EMAIL) && !emailValidado.contains("@")) {
            throw new IllegalArgumentException("Para alertar por email indicá una dirección válida");
        }
        if ((elegidos.contains(TipoNotificacion.SMS) || elegidos.contains(TipoNotificacion.WHATSAPP))
                && telefonoValidado.isEmpty()) {
            throw new IllegalArgumentException("Para alertar por SMS o WhatsApp indicá un teléfono");
        }
        this.canales = elegidos.stream().map(Enum::name).collect(Collectors.joining(","));
        this.email = emailValidado.isEmpty() ? null : emailValidado;
        this.telefono = telefonoValidado.isEmpty() ? null : telefonoValidado;
    }

    public static ConfiguracionAlertas porDefecto() {
        return new ConfiguracionAlertas(Set.of(TipoNotificacion.PANEL), null, null);
    }

    public Set<TipoNotificacion> getCanales() {
        return Arrays.stream(canales.split(","))
                .map(TipoNotificacion::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TipoNotificacion.class)));
    }

    /** Experto en información: conoce los contactos, entonces sabe a quién va cada canal. */
    public String destinatarioPara(TipoNotificacion canal) {
        return switch (canal) {
            case PANEL -> "Panel del establecimiento";
            case EMAIL -> email;
            case SMS, WHATSAPP -> telefono;
        };
    }

    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
}
