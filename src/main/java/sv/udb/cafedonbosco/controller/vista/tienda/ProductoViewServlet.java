package sv.udb.cafedonbosco.controller.vista.tienda;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.service.PersonalizacionService;
import sv.udb.cafedonbosco.service.ProductoService;
import sv.udb.cafedonbosco.service.impl.PersonalizacionServiceImpl;
import sv.udb.cafedonbosco.service.impl.ProductoServiceImpl;

import java.io.IOException;

@WebServlet(name = "TiendaProductoViewServlet", urlPatterns = "/tienda/producto")
public class ProductoViewServlet extends TiendaBaseServlet {

    private static final String VISTA = "/WEB-INF/views/tienda/producto.jsp";

    private final ProductoService productoService = new ProductoServiceImpl();
    private final PersonalizacionService personalizacionService = new PersonalizacionServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("activo", "menu");
        request.setAttribute("carritoUnidades", contarUnidadesCarrito(request));

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            request.setAttribute("producto", productoService.obtenerDetalle(id));
            request.setAttribute("relacionados", productoService.listarRelacionados(id, 4));
            request.setAttribute("gruposOpcion", personalizacionService.listarGruposDeProducto(id));
        } catch (NumberFormatException e) {
            request.setAttribute("error", "El producto solicitado no es valido.");
        } catch (RecursoNoEncontradoException e) {
            request.setAttribute("error", e.getMessage());
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }
}
