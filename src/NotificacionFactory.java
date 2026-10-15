public class NotificacionFactory {

    public static MedioNotificacion crear(String tipo) {

        switch (tipo.toUpperCase()) {

            case "EMAIL":
                return new Email();

            case "SMS":
                return new SMS();

            case "WHATSAPP":
                return new WhatsApp();

            default:
                throw new IllegalArgumentException("Medio de notificación inválido");
        }
    }
}