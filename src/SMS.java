public class SMS implements MedioNotificacion {

    @Override
    public void enviar(
            String destinatario,
            String mensaje) {

        System.out.println("SMS a " + destinatario + ": " + mensaje);
    }
}