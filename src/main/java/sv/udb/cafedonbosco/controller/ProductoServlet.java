package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.ProductoRequestDTO;
import sv.udb.cafedonbosco.dto.response.ProductoAdminResponseDTO;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.exception.AccesoDenegadoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.service.PersonalizacionService;
import sv.udb.cafedonbosco.service.ProductoService;
import sv.udb.cafedonbosco.service.impl.PersonalizacionServiceImpl;
import sv.udb.cafedonbosco.service.impl.ProductoServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;
import java.util.Map;

/**
 * Catalogo publico bajo /api/productos y gestion administrativa bajo
 * /api/admin/productos (protegida por RolAdminFilter). El consumidor
 * solo ve lo que expone ProductoResponseDTO (sin stock exacto); el
 * administrador usa ProductoAdminResponseDTO.
 */
@WebServlet(name = "ProductoServlet", urlPatterns = {
        "/api/productos", "/api/productos/*", "/api/admin/productos", "/api/admin/productos/*"
})
public class ProductoServlet extends BaseServlet {

    private final ProductoService productoService = new ProductoServiceImpl();
    private final PersonalizacionService personalizacionService = new PersonalizacionServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            boolean esAdmin = request.getServletPath().startsWith("/api/admin");
            String[] segmentos = segmentosDePath(request);

            if (segmentos.length == 0) {
                if (esAdmin) {
                    JsonUtil.exito(response, HttpServletResponse.SC_OK, "Productos obtenidos", productoService.listarAdmin());
                } else {
                    Integer categoriaId = parametroEntero(request, "categoria");
                    String busqueda = request.getParameter("buscar");
                    String orden = request.getParameter("orden");
                    JsonUtil.exito(response, HttpServletResponse.SC_OK, "Catalogo obtenido",
                            productoService.listarCatalogo(categoriaId, busqueda, orden));
                }
                return;
            }

            int id = Integer.parseInt(segmentos[0]);
            if (segmentos.length == 2 && "relacionados".equals(segmentos[1])) {
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Productos relacionados",
                        productoService.listarRelacionados(id, 4));
                return;
            }

            if (segmentos.length == 2 && "opciones".equals(segmentos[1])) {
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Opciones de personalizacion obtenidas",
                        personalizacionService.listarGruposDeProducto(id));
                return;
            }

            if (esAdmin) {
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Producto obtenido", productoService.obtenerDetalleAdmin(id));
            } else {
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Producto obtenido", productoService.obtenerDetalle(id));
            }
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id del producto no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UsuarioResponseDTO administrador = exigirAdministrador(request);
            ProductoRequestDTO datos = JsonUtil.leerCuerpo(request, ProductoRequestDTO.class);
            ProductoAdminResponseDTO creado = productoService.crear(datos, administrador.getId());
            JsonUtil.exito(response, HttpServletResponse.SC_CREATED, "Producto creado correctamente", creado);
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UsuarioResponseDTO administrador = exigirAdministrador(request);
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length == 0) {
                throw new ValidacionException("Debes indicar el id del producto en la URL.");
            }
            int id = Integer.parseInt(segmentos[0]);

            if (segmentos.length == 2 && "estado".equals(segmentos[1])) {
                Map<String, Object> cuerpo = JsonUtil.leerCuerpo(request, Map.class);
                boolean activo = cuerpo != null && Boolean.TRUE.equals(cuerpo.get("activo"));
                productoService.cambiarEstado(id, activo, administrador.getId());
                JsonUtil.exito(response, HttpServletResponse.SC_OK,
                        activo ? "Producto activado" : "Producto desactivado", null);
                return;
            }

            ProductoRequestDTO datos = JsonUtil.leerCuerpo(request, ProductoRequestDTO.class);
            ProductoAdminResponseDTO actualizado = productoService.actualizar(id, datos, administrador.getId());
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Producto actualizado correctamente", actualizado);
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id del producto no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    /**
     * ProductoServlet atiende tanto /api/productos (catalogo publico,
     * solo lectura) como /api/admin/productos (gestion completa). Solo
     * RolAdminFilter protege el prefijo /api/admin/*, asi que sin esta
     * comprobacion una escritura enviada a la ruta publica se ejecutaria
     * sin sesion de administrador.
     */
    private UsuarioResponseDTO exigirAdministrador(HttpServletRequest request) {
        if (!request.getServletPath().startsWith("/api/admin")) {
            throw new AccesoDenegadoException("Esta operacion solo esta disponible para administradores.");
        }
        UsuarioResponseDTO administrador = SessionUtil.obtenerUsuarioAutenticado(request);
        if (administrador == null) {
            throw new AccesoDenegadoException("Debes iniciar sesion como administrador.");
        }
        return administrador;
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
}
