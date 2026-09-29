package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.service.DashboardService;
import sv.udb.cafedonbosco.service.impl.DashboardServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;

import java.io.IOException;

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/admin/dashboard")
public class DashboardServlet extends BaseServlet {

    private final DashboardService dashboardService = new DashboardServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Resumen del dashboard obtenido", dashboardService.obtenerResumen());
        } catch (Exception e) {
            manejarError(response, e);
        }
    }
}
