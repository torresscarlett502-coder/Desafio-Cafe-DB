package sv.udb.cafedonbosco.controller.vista.tienda;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.service.VentaService;
import sv.udb.cafedonbosco.service.impl.VentaServiceImpl;

import java.io.IOException;

/**
 * Se accede con el token aleatorio de la venta recien creada (nunca con
 * el id incremental), para que nadie mas pueda ver los datos de otro
 * comprador cambiando un numero en la URL.
 */
@WebServlet(name = "TiendaConfirmacionViewServlet", urlPatterns = "/tienda/confirmacion")
public class ConfirmacionViewServlet extends TiendaBaseServlet {

    private static final String VISTA = "/WEB-INF/views/tienda/confirmacion.jsp";

    private final VentaService ventaService = new VentaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("carritoUnidades", contarUnidadesCarrito(request));

        String token = request.getParameter("token");
        try {
            request.setAttribute("venta", ventaService.obtenerPorToken(token));
        } catch (RecursoNoEncontradoException e) {
            request.setAttribute("error", e.getMessage());
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }
}
