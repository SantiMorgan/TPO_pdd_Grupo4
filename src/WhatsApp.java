public class WhatsApp implements MedioNotificacion {

    @Override
    public void enviar(
            String destinatario,
            String mensaje) {

        System.out.println(
                "WHATSAPP a " + destinatario
                        + ": " + mensaje
        );
    }
}
