package sv.udb.cafedonbosco.controller.vista.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.service.CategoriaService;
import sv.udb.cafedonbosco.service.ProductoService;
import sv.udb.cafedonbosco.service.impl.CategoriaServiceImpl;
import sv.udb.cafedonbosco.service.impl.ProductoServiceImpl;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 * Listado administrativo de productos: muestra stock exacto y estado
 * real (activo/inactivo), a diferencia del catalogo publico de la
 * tienda. Solo renderiza la pagina; crear, editar y activar/desactivar
 * un producto se hace desde el navegador contra /api/admin/productos
 * (ProductoServlet), ya protegido por RolAdminFilter.
 */
@WebServlet(name = "AdminProductosViewServlet", urlPatterns = "/admin/productos")
public class ProductosViewServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/admin/productos.jsp";

    private final ProductoService productoService = new ProductoServiceImpl();
    private final CategoriaService categoriaService = new CategoriaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("activo", "productos");
        request.setAttribute("usuario", SessionUtil.obtenerUsuarioAutenticado(request));
        request.setAttribute("productos", productoService.listarAdmin());
        request.setAttribute("categorias", categoriaService.listarActivas());

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }
}
