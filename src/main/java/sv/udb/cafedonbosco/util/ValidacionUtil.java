package sv.udb.cafedonbosco.util;

import java.util.regex.Pattern;

public final class ValidacionUtil {

    // El dominio admite cualquier cantidad de subdominios antes del TLD
    // (ej. usuario@mail.udb.edu.sv); una version anterior de este patron
    // solo aceptaba un dominio con un unico punto y rechazaba correos
    // institucionales reales con subdominios.
    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-zA-Z]{2,}$");

    private static final Pattern PATRON_TELEFONO =
            Pattern.compile("^[+]?[0-9\\s-]{7,20}$");

    private ValidacionUtil() {
    }

    public static boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static boolean esTextoValido(String texto, int longitudMaxima) {
        return esTextoValido(texto) && texto.trim().length() <= longitudMaxima;
    }

    public static boolean esCorreoValido(String correo) {
        return esTextoValido(correo) && correo.length() <= 120 && PATRON_CORREO.matcher(correo).matches();
    }

    public static boolean esTelefonoValido(String telefono) {
        return esTextoValido(telefono) && PATRON_TELEFONO.matcher(telefono).matches();
    }

    public static boolean esCantidadValida(Integer cantidad) {
        return cantidad != null && cantidad > 0;
    }
}
