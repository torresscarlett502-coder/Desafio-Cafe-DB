package sv.udb.cafedonbosco.exception;

import jakarta.servlet.http.HttpServletResponse;

public class StockInsuficienteException extends AppException {

    public StockInsuficienteException(String nombreProducto) {
        super("Stock insuficiente para el producto: " + nombreProducto, HttpServletResponse.SC_CONFLICT);
    }
}
