package sv.udb.cafedonbosco.service;

public interface EmailService {

    /**
     * Encola el envio del ticket en PDF por correo y regresa de inmediato:
     * el envio real ocurre en un hilo aparte (ver EmailExecutor) para no
     * bloquear la solicitud HTTP con la latencia del SMTP. Si el SMTP no
     * esta configurado, el intento se registra en el log del servidor en
     * vez de lanzar un error al usuario.
     */
    void enviarTicketPorCorreo(String destinatario, byte[] pdfTicket, String numeroTicket);
}
