package sv.udb.cafedonbosco.exception;

import jakarta.servlet.http.HttpServletResponse;

/** Conflicto por unicidad: correo repetido, nombre de producto duplicado, etc. */
public class RecursoDuplicadoException extends AppException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje, HttpServletResponse.SC_CONFLICT);
    }
}
