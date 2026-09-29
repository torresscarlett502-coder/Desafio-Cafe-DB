package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.ActualizarPerfilRequestDTO;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.exception.AccesoDenegadoException;
import sv.udb.cafedonbosco.service.AuthService;
import sv.udb.cafedonbosco.service.impl.AuthServiceImpl;
import sv.udb.cafedonbosco.util.Constantes;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 * Perfil de la cuenta del usuario con sesion iniciada (consumidor o
 * administrador): GET consulta sus datos, PUT actualiza nombre/apellido/
 * correo. La sesion se refresca con los datos nuevos para que el resto
 * de la aplicacion (que lee SessionUtil.obtenerUsuarioAutenticado) los
 * vea de inmediato sin tener que volver a iniciar sesion.
 */
@WebServlet(name = "MiCuentaServlet", urlPatterns = "/api/mi-cuenta")
public class MiCuentaServlet extends BaseServlet {

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UsuarioResponseDTO usuario = requerirSesion(request);
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Perfil obtenido",
                    authService.obtenerPerfil(usuario.getId()));
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UsuarioResponseDTO usuario = requerirSesion(request);
            ActualizarPerfilRequestDTO datos = JsonUtil.leerCuerpo(request, ActualizarPerfilRequestDTO.class);
            UsuarioResponseDTO actualizado = authService.actualizarPerfil(usuario.getId(), datos);
            request.getSession(true).setAttribute(Constantes.SESSION_USUARIO, actualizado);
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Perfil actualizado correctamente", actualizado);
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    private UsuarioResponseDTO requerirSesion(HttpServletRequest request) {
        UsuarioResponseDTO usuario = SessionUtil.obtenerUsuarioAutenticado(request);
        if (usuario == null) {
            throw new AccesoDenegadoException("Debes iniciar sesion para ver o editar tu cuenta.");
        }
        return usuario;
    }
}
