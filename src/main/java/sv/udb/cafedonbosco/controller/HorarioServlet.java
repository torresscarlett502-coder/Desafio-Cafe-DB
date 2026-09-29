package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.HorarioAtencionRequestDTO;
import sv.udb.cafedonbosco.exception.AccesoDenegadoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.service.HorarioAtencionService;
import sv.udb.cafedonbosco.service.impl.HorarioAtencionServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 *   GET /api/horario/estado         publico: si el local esta abierto ahora
 *   GET /api/admin/horario          admin: los 7 dias configurados
 *   PUT /api/admin/horario/{dia}    admin: actualiza un dia (1=lunes..7=domingo)
 */
@WebServlet(name = "HorarioServlet", urlPatterns = {"/api/horario/estado", "/api/admin/horario", "/api/admin/horario/*"})
public class HorarioServlet extends BaseServlet {

    private final HorarioAtencionService horarioService = new HorarioAtencionServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (request.getServletPath().startsWith("/api/admin")) {
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Horarios obtenidos", horarioService.listarTodos());
                return;
            }
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Estado del local obtenido", horarioService.obtenerEstadoActual());
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (!request.getServletPath().startsWith("/api/admin")) {
                throw new AccesoDenegadoException("Esta operacion solo esta disponible para administradores.");
            }
            if (SessionUtil.obtenerUsuarioAutenticado(request) == null) {
                throw new AccesoDenegadoException("Debes iniciar sesion como administrador.");
            }
            int diaSemana = extraerDiaSemana(request);
            HorarioAtencionRequestDTO datos = JsonUtil.leerCuerpo(request, HorarioAtencionRequestDTO.class);
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Horario actualizado correctamente",
                    horarioService.actualizar(diaSemana, datos));
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El dia de la semana no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    private int extraerDiaSemana(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            throw new ValidacionException("Debes indicar el dia de la semana en la URL (1=lunes..7=domingo).");
        }
        return Integer.parseInt(pathInfo.substring(1));
    }
}
