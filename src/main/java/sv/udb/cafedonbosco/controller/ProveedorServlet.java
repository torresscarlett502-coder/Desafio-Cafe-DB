package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.ProveedorRequestDTO;
import sv.udb.cafedonbosco.dto.response.ProveedorResponseDTO;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.service.ProveedorService;
import sv.udb.cafedonbosco.service.impl.ProveedorServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;

import java.io.IOException;
import java.util.List;

/**
 * Gestion de proveedores, siempre bajo /api/admin (protegido por
 * RolAdminFilter): los proveedores solo le interesan al administrador al
 * registrar una compra, nunca al consumidor.
 *   GET  /api/admin/proveedores            todos (activos e inactivos)
 *   GET  /api/admin/proveedores/{id}       detalle
 *   GET  /api/admin/proveedores?activos=1  solo activos (para el selector de la compra)
 *   POST /api/admin/proveedores            crear
 *   PUT  /api/admin/proveedores/{id}       actualizar
 */
@WebServlet(name = "ProveedorServlet", urlPatterns = {"/api/admin/proveedores", "/api/admin/proveedores/*"})
public class ProveedorServlet extends BaseServlet {

    private final ProveedorService proveedorService = new ProveedorServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length == 0) {
                boolean soloActivos = "1".equals(request.getParameter("activos"))
                        || "true".equalsIgnoreCase(request.getParameter("activos"));
                List<ProveedorResponseDTO> proveedores = soloActivos
                        ? proveedorService.listarActivos()
                        : proveedorService.listarTodos();
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Proveedores obtenidos", proveedores);
                return;
            }
            int id = Integer.parseInt(segmentos[0]);
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Proveedor obtenido", proveedorService.obtenerPorId(id));
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id del proveedor no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ProveedorRequestDTO datos = JsonUtil.leerCuerpo(request, ProveedorRequestDTO.class);
            ProveedorResponseDTO creado = proveedorService.crear(datos);
            JsonUtil.exito(response, HttpServletResponse.SC_CREATED, "Proveedor creado correctamente", creado);
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length == 0) {
                throw new ValidacionException("Debes indicar el id del proveedor en la URL.");
            }
            int id = Integer.parseInt(segmentos[0]);
            ProveedorRequestDTO datos = JsonUtil.leerCuerpo(request, ProveedorRequestDTO.class);
            ProveedorResponseDTO actualizado = proveedorService.actualizar(id, datos);
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Proveedor actualizado correctamente", actualizado);
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id del proveedor no es valido.");
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
}
