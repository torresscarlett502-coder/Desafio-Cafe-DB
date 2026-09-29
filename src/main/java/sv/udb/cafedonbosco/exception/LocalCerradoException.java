package sv.udb.cafedonbosco.exception;

import jakarta.servlet.http.HttpServletResponse;

/** El local esta cerrado (fuera de horario, o el canal esta deshabilitado ese dia): no se acepta el pedido. */
public class LocalCerradoException extends AppException {

    public LocalCerradoException(String mensaje) {
        super(mensaje, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
}
