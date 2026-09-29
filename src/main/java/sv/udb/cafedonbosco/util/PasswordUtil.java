package sv.udb.cafedonbosco.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hashear(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(12));
    }

    public static boolean verificar(String passwordPlano, String passwordHasheado) {
        if (passwordPlano == null || passwordHasheado == null) {
            return false;
        }
        return BCrypt.checkpw(passwordPlano, passwordHasheado);
    }
}
