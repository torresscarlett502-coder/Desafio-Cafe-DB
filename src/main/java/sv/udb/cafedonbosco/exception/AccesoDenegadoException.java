package sv.udb.cafedonbosco.exception;

import jakarta.servlet.http.HttpServletResponse;

public class AccesoDenegadoException extends AppException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje, HttpServletResponse.SC_FORBIDDEN);
    }
}
