package sv.udb.cafedonbosco.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Genera enlaces "click-to-chat" de WhatsApp (wa.me): al abrirlos, WhatsApp
 * se abre con el mensaje ya escrito en la conversacion indicada (o dejando
 * que quien lo abre elija el contacto, si no se indica numero). Esto NO es
 * un envio automatico: WhatsApp no ofrece una forma de que un servidor
 * mande mensajes sin la API de WhatsApp Business (que requiere una cuenta
 * de Meta Business, plantillas aprobadas, etc.), asi que esto es lo maximo
 * que un backend como este puede hacer de forma honesta: darle al usuario
 * un enlace listo para compartir.
 */
public final class WhatsAppUtil {

    private WhatsAppUtil() {
    }

    /** telefono en formato internacional sin signos (ej. "50370000000"); null o vacio deja que el usuario elija el contacto. */
    public static String construirEnlace(String telefono, String mensaje) {
        String mensajeCodificado = codificar(mensaje);
        if (telefono == null || telefono.isBlank()) {
            return "https://wa.me/?text=" + mensajeCodificado;
        }
        String telefonoLimpio = telefono.replaceAll("[^0-9]", "");
        return "https://wa.me/" + telefonoLimpio + "?text=" + mensajeCodificado;
    }

    private static String codificar(String texto) {
        try {
            return URLEncoder.encode(texto != null ? texto : "", StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 no soportado", e);
        }
    }
}
