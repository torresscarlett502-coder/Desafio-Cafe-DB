package sv.udb.cafedonbosco.controller.vista.tienda;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.response.ProductoResponseDTO;
import sv.udb.cafedonbosco.service.CategoriaService;
import sv.udb.cafedonbosco.service.ProductoService;
import sv.udb.cafedonbosco.service.impl.CategoriaServiceImpl;
import sv.udb.cafedonbosco.service.impl.ProductoServiceImpl;

import java.io.IOException;
import java.util.List;

/**
 * Home de la tienda del consumidor. No requiere sesion: el consumidor
 * entra directo desde el portal, sin cuenta ni login.
 */
@WebServlet(name = "TiendaInicioViewServlet", urlPatterns = "/tienda")
public class InicioViewServlet extends TiendaBaseServlet {

    private static final String VISTA = "/WEB-INF/views/tienda/inicio.jsp";

    private final ProductoService productoService = new ProductoServiceImpl();
    private final CategoriaService categoriaService = new CategoriaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("activo", "inicio");
        request.setAttribute("carritoUnidades", contarUnidadesCarrito(request));

        List<ProductoResponseDTO> destacados = productoService.listarCatalogo(null, null, "nombre");
        request.setAttribute("destacados", destacados.subList(0, Math.min(4, destacados.size())));
        request.setAttribute("categorias", categoriaService.listarActivas());

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }
}
