package sv.udb.cafedonbosco.exception;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Envuelve errores tecnicos (SQL, IO, etc.) que no deben exponerse al
 * cliente con su detalle original.
 */
public class ErrorInternoException extends AppException {

    public ErrorInternoException(String mensaje, Throwable causa) {
        super(mensaje, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        initCause(causa);
    }
}
