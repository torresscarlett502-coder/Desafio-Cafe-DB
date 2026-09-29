package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.InventarioAjusteRequestDTO;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.exception.AccesoDenegadoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.service.InventarioService;
import sv.udb.cafedonbosco.service.impl.InventarioServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 *   GET /api/admin/inventario               inventario actual por producto
 *   GET /api/admin/inventario/movimientos   historial de movimientos (auditoria)
 *   PUT /api/admin/inventario               ajuste manual de stock/stock minimo
 */
@WebServlet(name = "InventarioServlet", urlPatterns = {"/api/admin/inventario", "/api/admin/inventario/*"})
public class InventarioServlet extends BaseServlet {

    private final InventarioService inventarioService = new InventarioServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length == 1 && "movimientos".equals(segmentos[0])) {
                Integer productoId = parametroEntero(request, "productoId");
                int limite = parametroLimite(request);
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Movimientos de inventario obtenidos",
                        inventarioService.listarMovimientos(productoId, limite));
                return;
            }
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Inventario obtenido", inventarioService.listarInventario());
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UsuarioResponseDTO administrador = SessionUtil.obtenerUsuarioAutenticado(request);
            if (administrador == null) {
                throw new AccesoDenegadoException("Debes iniciar sesion como administrador.");
            }
            InventarioAjusteRequestDTO datos = JsonUtil.leerCuerpo(request, InventarioAjusteRequestDTO.class);
            inventarioService.ajustar(datos, administrador.getId());
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Inventario actualizado correctamente",
                    inventarioService.listarInventario());
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    private String[] segmentosDePath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return new String[0];
        }
        String limpio = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        return limpio.split("/");
    }

    private Integer parametroEntero(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new ValidacionException("El parametro " + nombre + " debe ser numerico.");
        }
    }

    private int parametroLimite(HttpServletRequest request) {
        String valor = request.getParameter("limite");
        if (valor == null || valor.isBlank()) {
            return 50;
        }
        try {
            return Math.max(1, Integer.parseInt(valor));
        } catch (NumberFormatException e) {
            return 50;
        }
    }
}
