package sv.udb.cafedonbosco.exception;

import jakarta.servlet.http.HttpServletResponse;

public class CredencialesInvalidasException extends AppException {

    public CredencialesInvalidasException() {
        super("Correo o contrasena incorrectos", HttpServletResponse.SC_UNAUTHORIZED);
    }
}
