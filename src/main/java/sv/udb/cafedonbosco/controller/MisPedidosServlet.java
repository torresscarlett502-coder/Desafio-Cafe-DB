package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.exception.AccesoDenegadoException;
import sv.udb.cafedonbosco.service.VentaService;
import sv.udb.cafedonbosco.service.impl.VentaServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 * Historial de pedidos del consumidor que si tiene cuenta y sesion
 * iniciada (el checkout como invitado sigue funcionando sin esto).
 *   GET /api/mis-pedidos       pedidos del usuario autenticado
 *   GET /api/mis-pedidos/{id}  detalle de uno de sus pedidos
 */
@WebServlet(name = "MisPedidosServlet", urlPatterns = {"/api/mis-pedidos", "/api/mis-pedidos/*"})
public class MisPedidosServlet extends BaseServlet {

    private final VentaService ventaService = new VentaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UsuarioResponseDTO usuario = SessionUtil.obtenerUsuarioAutenticado(request);
            if (usuario == null) {
                throw new AccesoDenegadoException("Debes iniciar sesion para ver tus pedidos.");
            }

            String[] segmentos = segmentosDePath(request);
            if (segmentos.length == 0) {
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Pedidos obtenidos",
                        ventaService.listarPedidosDeUsuario(usuario.getId(), 50));
                return;
            }

            int id = Integer.parseInt(segmentos[0]);
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Pedido obtenido",
                    ventaService.obtenerPedidoDeUsuario(usuario.getId(), id));
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id del pedido no es valido.");
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
