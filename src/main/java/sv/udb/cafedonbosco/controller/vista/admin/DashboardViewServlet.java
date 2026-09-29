package sv.udb.cafedonbosco.controller.vista.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.response.ProductoResponseDTO;
import sv.udb.cafedonbosco.service.DashboardService;
import sv.udb.cafedonbosco.service.ProductoService;
import sv.udb.cafedonbosco.service.impl.DashboardServiceImpl;
import sv.udb.cafedonbosco.service.impl.ProductoServiceImpl;
import sv.udb.cafedonbosco.util.FechaUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminDashboardViewServlet", urlPatterns = "/admin/dashboard")
public class DashboardViewServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/admin/dashboard.jsp";

    private final DashboardService dashboardService = new DashboardServiceImpl();
    private final ProductoService productoService = new ProductoServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("activo", "dashboard");
        request.setAttribute("usuario", SessionUtil.obtenerUsuarioAutenticado(request));
        request.setAttribute("resumen", dashboardService.obtenerResumen());
        request.setAttribute("saludo", FechaUtil.obtenerSaludo());

        List<ProductoResponseDTO> destacados = productoService.listarCatalogo(null, null, "nombre");
        request.setAttribute("destacados", destacados.subList(0, Math.min(4, destacados.size())));

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }
}
