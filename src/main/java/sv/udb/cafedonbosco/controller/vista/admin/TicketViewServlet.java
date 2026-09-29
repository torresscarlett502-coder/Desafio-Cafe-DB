package sv.udb.cafedonbosco.controller.vista.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.service.VentaService;
import sv.udb.cafedonbosco.service.impl.VentaServiceImpl;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 * Ticket de cualquier venta, consultado por id (el administrador ya esta
 * autenticado, a diferencia del consumidor que debe usar el token
 * aleatorio en /tienda/ticket).
 */
@WebServlet(name = "AdminTicketViewServlet", urlPatterns = "/admin/ticket")
public class TicketViewServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/admin/ticket.jsp";

    private final VentaService ventaService = new VentaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("usuario", SessionUtil.obtenerUsuarioAutenticado(request));
        request.setAttribute("esNueva", "1".equals(request.getParameter("nueva")));

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            request.setAttribute("venta", ventaService.obtenerPorId(id));
        } catch (NumberFormatException e) {
            request.setAttribute("error", "El id de la venta no es valido.");
        } catch (RecursoNoEncontradoException e) {
            request.setAttribute("error", e.getMessage());
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }
}
