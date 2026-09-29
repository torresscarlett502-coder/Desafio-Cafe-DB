package sv.udb.cafedonbosco.exception;

import jakarta.servlet.http.HttpServletResponse;

public class ValidacionException extends AppException {

    public ValidacionException(String mensaje) {
        super(mensaje, HttpServletResponse.SC_BAD_REQUEST);
    }
}
