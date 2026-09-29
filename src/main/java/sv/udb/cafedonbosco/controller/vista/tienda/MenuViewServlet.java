package sv.udb.cafedonbosco.controller.vista.tienda;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.service.CategoriaService;
import sv.udb.cafedonbosco.service.ProductoService;
import sv.udb.cafedonbosco.service.impl.CategoriaServiceImpl;
import sv.udb.cafedonbosco.service.impl.ProductoServiceImpl;

import java.io.IOException;

/**
 * Catalogo completo de la tienda, con busqueda, filtro por categoria y
 * orden. Publico, sin sesion: el consumidor entra directo desde el
 * portal.
 */
@WebServlet(name = "TiendaMenuViewServlet", urlPatterns = "/tienda/menu")
public class MenuViewServlet extends TiendaBaseServlet {

    private static final String VISTA = "/WEB-INF/views/tienda/menu.jsp";

    private final ProductoService productoService = new ProductoServiceImpl();
    private final CategoriaService categoriaService = new CategoriaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("activo", "menu");
        request.setAttribute("carritoUnidades", contarUnidadesCarrito(request));

        Integer categoriaId = parametroEntero(request.getParameter("categoria"));
        String busqueda = request.getParameter("buscar");
        String orden = request.getParameter("orden");

        request.setAttribute("categoriaSeleccionada", categoriaId);
        request.setAttribute("busqueda", busqueda);
        request.setAttribute("orden", orden);
        request.setAttribute("categorias", categoriaService.listarActivas());
        request.setAttribute("productos", productoService.listarCatalogo(categoriaId, busqueda, orden));
        request.setAttribute("totalProductos", productoService.listarCatalogo(null, null, null).size());

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }

    private Integer parametroEntero(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
