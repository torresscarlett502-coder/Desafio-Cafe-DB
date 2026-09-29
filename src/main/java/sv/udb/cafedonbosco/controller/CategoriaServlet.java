package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.CategoriaRequestDTO;
import sv.udb.cafedonbosco.dto.response.CategoriaResponseDTO;
import sv.udb.cafedonbosco.exception.AccesoDenegadoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.service.CategoriaService;
import sv.udb.cafedonbosco.service.impl.CategoriaServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 * GET /api/categorias es publico y solo muestra categorias activas
 * (catalogo del consumidor). Las rutas /api/admin/categorias estan
 * protegidas por RolAdminFilter y permiten ver todas, crear y editar.
 */
@WebServlet(name = "CategoriaServlet", urlPatterns = {
        "/api/categorias", "/api/admin/categorias", "/api/admin/categorias/*"
})
public class CategoriaServlet extends BaseServlet {

    private final CategoriaService categoriaService = new CategoriaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            boolean esAdmin = request.getServletPath().startsWith("/api/admin");
            var categorias = esAdmin ? categoriaService.listarTodas() : categoriaService.listarActivas();
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Categorias obtenidas", categorias);
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            exigirAdministrador(request);
            CategoriaRequestDTO datos = JsonUtil.leerCuerpo(request, CategoriaRequestDTO.class);
            CategoriaResponseDTO creada = categoriaService.crear(datos);
            JsonUtil.exito(response, HttpServletResponse.SC_CREATED, "Categoria creada correctamente", creada);
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            exigirAdministrador(request);
            int id = extraerId(request);
            CategoriaRequestDTO datos = JsonUtil.leerCuerpo(request, CategoriaRequestDTO.class);
            CategoriaResponseDTO actualizada = categoriaService.actualizar(id, datos);
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Categoria actualizada correctamente", actualizada);
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    /**
     * Este servlet atiende tanto /api/categorias (catalogo publico, solo
     * lectura) como /api/admin/categorias (gestion completa). Solo
     * RolAdminFilter protege el prefijo /api/admin/*, asi que sin esta
     * comprobacion una escritura enviada a la ruta publica se ejecutaria
     * sin sesion de administrador.
     */
    private void exigirAdministrador(HttpServletRequest request) {
        if (!request.getServletPath().startsWith("/api/admin")) {
            throw new AccesoDenegadoException("Esta operacion solo esta disponible para administradores.");
        }
        if (SessionUtil.obtenerUsuarioAutenticado(request) == null) {
            throw new AccesoDenegadoException("Debes iniciar sesion como administrador.");
        }
    }

    private int extraerId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            throw new ValidacionException("Debes indicar el id de la categoria en la URL.");
        }
        try {
            return Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            throw new ValidacionException("El id de la categoria no es valido.");
        }
    }
}
