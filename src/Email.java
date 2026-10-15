public class Email implements MedioNotificacion {

    @Override
    public void enviar(
            String destinatario,
            String mensaje) {

        System.out.println("EMAIL a " + destinatario + ": " + mensaje);
    }
}

