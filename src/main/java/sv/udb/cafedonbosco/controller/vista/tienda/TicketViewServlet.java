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
 * Ticket del consumidor: se consulta por el token aleatorio de la venta,
 * nunca por el id incremental, para que un comprador no pueda ver el
 * comprobante de otro cambiando un numero en la URL.
 */
@WebServlet(name = "TiendaTicketViewServlet", urlPatterns = "/tienda/ticket")
public class TicketViewServlet extends TiendaBaseServlet {

    private static final String VISTA = "/WEB-INF/views/tienda/ticket.jsp";

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
