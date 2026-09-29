package sv.udb.cafedonbosco.service.impl;

import jakarta.activation.DataHandler;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sv.udb.cafedonbosco.service.EmailService;
import sv.udb.cafedonbosco.util.EmailExecutor;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Configuracion por variables de entorno (nunca credenciales en el
 * repositorio): SMTP_HOST, SMTP_PUERTO, SMTP_USUARIO, SMTP_PASSWORD,
 * SMTP_REMITENTE. Si SMTP_HOST no esta definido, el envio se omite y se
 * registra en el log en vez de fallar la operacion que lo disparo (el
 * checkout no debe romperse porque el correo no este configurado en un
 * entorno de pruebas).
 */
public class EmailServiceImpl implements EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static final String HOST = System.getenv().getOrDefault("SMTP_HOST", "");
    private static final String PUERTO = System.getenv().getOrDefault("SMTP_PUERTO", "587");
    private static final String USUARIO = System.getenv().getOrDefault("SMTP_USUARIO", "");
    private static final String PASSWORD = System.getenv().getOrDefault("SMTP_PASSWORD", "");
    private static final String REMITENTE = System.getenv().getOrDefault("SMTP_REMITENTE", "no-responder@cafedonbosco.com");

    @Override
    public void enviarTicketPorCorreo(String destinatario, byte[] pdfTicket, String numeroTicket) {
        EmailExecutor.ejecutar(() -> enviarAhora(destinatario, pdfTicket, numeroTicket));
    }

    private void enviarAhora(String destinatario, byte[] pdfTicket, String numeroTicket) {
        if (HOST.isBlank()) {
            LOG.warn("SMTP_HOST no esta configurado; se omite el envio del ticket #{} a {}", numeroTicket, destinatario);
            return;
        }
        try {
            Session sesion = Session.getInstance(propiedadesSmtp(), new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USUARIO, PASSWORD);
                }
            });

            MimeMessage mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(REMITENTE, "Cafe Don Bosco"));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject("Tu ticket de Cafe Don Bosco - Pedido #" + numeroTicket);

            MimeBodyPart cuerpo = new MimeBodyPart();
            cuerpo.setText("Gracias por tu compra en Cafe Don Bosco.\n\n"
                    + "Adjuntamos el comprobante de tu pedido #" + numeroTicket + " en PDF.\n\n"
                    + "Cafe Don Bosco - Buen cafe, mejores momentos.");

            MimeBodyPart adjunto = new MimeBodyPart();
            adjunto.setDataHandler(new DataHandler(new ByteArrayDataSource(pdfTicket, "application/pdf")));
            adjunto.setFileName("ticket-" + numeroTicket + ".pdf");

            MimeMultipart contenido = new MimeMultipart();
            contenido.addBodyPart(cuerpo);
            contenido.addBodyPart(adjunto);
            mensaje.setContent(contenido);

            Transport.send(mensaje);
            LOG.info("Ticket #{} enviado por correo a {}", numeroTicket, destinatario);
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOG.error("Error al enviar el ticket #{} por correo a {}", numeroTicket, destinatario, e);
        }
    }

    private Properties propiedadesSmtp() {
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", HOST);
        propiedades.put("mail.smtp.port", PUERTO);
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        return propiedades;
    }
}
