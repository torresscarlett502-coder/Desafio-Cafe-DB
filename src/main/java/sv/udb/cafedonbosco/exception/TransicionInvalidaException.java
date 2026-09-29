package sv.udb.cafedonbosco.exception;

import jakarta.servlet.http.HttpServletResponse;

/** Cambio de estado no permitido (ej. ENTREGADO -> EN_PREPARACION) o venta ya en estado terminal. */
public class TransicionInvalidaException extends AppException {

    public TransicionInvalidaException(String mensaje) {
        super(mensaje, HttpServletResponse.SC_CONFLICT);
    }
}
