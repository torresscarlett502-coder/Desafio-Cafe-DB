package sv.udb.cafedonbosco.controller.vista.tienda;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "TiendaNosotrosViewServlet", urlPatterns = "/tienda/nosotros")
public class NosotrosViewServlet extends TiendaBaseServlet {

    private static final String VISTA = "/WEB-INF/views/tienda/nosotros.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("activo", "nosotros");
        request.setAttribute("carritoUnidades", contarUnidadesCarrito(request));

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }
}
