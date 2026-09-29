package sv.udb.cafedonbosco.exception;

import jakarta.servlet.http.HttpServletResponse;

public class RecursoNoEncontradoException extends AppException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje, HttpServletResponse.SC_NOT_FOUND);
    }
}
