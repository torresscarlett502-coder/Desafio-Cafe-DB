package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sv.udb.cafedonbosco.dto.request.LoginRequestDTO;
import sv.udb.cafedonbosco.dto.request.RegistroConsumidorDTO;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.service.AuthService;
import sv.udb.cafedonbosco.service.impl.AuthServiceImpl;
import sv.udb.cafedonbosco.util.Constantes;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;


@WebServlet(name = "AuthServlet", urlPatterns = {
        "/api/auth/login", "/api/auth/registro", "/api/auth/logout", "/api/auth/sesion"
})
public class AuthServlet extends BaseServlet {

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String ruta = request.getServletPath();
            if (ruta.endsWith("/login")) {
                login(request, response);
            } else if (ruta.endsWith("/registro")) {
                registro(request, response);
            } else if (ruta.endsWith("/logout")) {
                logout(request, response);
            } else {
                JsonUtil.error(response, HttpServletResponse.SC_NOT_FOUND, "Ruta no encontrada.");
            }
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UsuarioResponseDTO usuario = SessionUtil.obtenerUsuarioAutenticado(request);
            if (usuario == null) {
                JsonUtil.error(response, HttpServletResponse.SC_UNAUTHORIZED, "No hay una sesion activa.");
                return;
            }
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Sesion activa", usuario);
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LoginRequestDTO datos = JsonUtil.leerCuerpo(request, LoginRequestDTO.class);
        UsuarioResponseDTO usuario = authService.login(
                datos != null ? datos.getCorreo() : null,
                datos != null ? datos.getPassword() : null,
                null
        );
        HttpSession sesion = iniciarSesionSegura(request);
        sesion.setAttribute(Constantes.SESSION_USUARIO, usuario);
        JsonUtil.exito(response, HttpServletResponse.SC_OK, "Bienvenido, " + usuario.getNombre(), usuario);
    }

    private void registro(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RegistroConsumidorDTO datos = JsonUtil.leerCuerpo(request, RegistroConsumidorDTO.class);
        UsuarioResponseDTO usuario = authService.registrarConsumidor(datos);
        HttpSession sesion = iniciarSesionSegura(request);
        sesion.setAttribute(Constantes.SESSION_USUARIO, usuario);
        JsonUtil.exito(response, HttpServletResponse.SC_CREATED, "Cuenta creada correctamente", usuario);
    }


    private HttpSession iniciarSesionSegura(HttpServletRequest request) {
        HttpSession sesion = request.getSession(true);
        request.changeSessionId();
        return sesion;
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        JsonUtil.exito(response, HttpServletResponse.SC_OK, "Sesion finalizada", null);
    }
}
