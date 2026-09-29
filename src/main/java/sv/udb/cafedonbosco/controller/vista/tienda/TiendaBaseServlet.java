package sv.udb.cafedonbosco.controller.vista.tienda;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import sv.udb.cafedonbosco.model.Carrito;
import sv.udb.cafedonbosco.util.SessionUtil;

/**
 * Todas las paginas de la tienda muestran el contador de unidades del
 * carrito en el encabezado; este helper evita repetir la misma consulta
 * de sesion en cada servlet.
 */
public abstract class TiendaBaseServlet extends HttpServlet {

    protected int contarUnidadesCarrito(HttpServletRequest request) {
        Carrito carrito = SessionUtil.obtenerOCrearCarrito(request);
        return carrito.contarUnidades();
    }
}
