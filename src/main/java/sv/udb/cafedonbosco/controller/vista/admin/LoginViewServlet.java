package sv.udb.cafedonbosco.controller.vista.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.exception.AppException;
import sv.udb.cafedonbosco.model.Rol;
import sv.udb.cafedonbosco.service.AuthService;
import sv.udb.cafedonbosco.service.impl.AuthServiceImpl;
import sv.udb.cafedonbosco.util.Constantes;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 * Login exclusivo del panel de administrador. El consumidor nunca pasa
 * por aqui: entra directo a la tienda desde el portal, sin cuenta ni
 * contrasena. Por eso el login exige explicitamente Rol.ADMINISTRADOR
 * en vez de aceptar cualquier usuario valido.
 */
@WebServlet(name = "AdminLoginViewServlet", urlPatterns = "/login")
public class LoginViewServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/admin/login.jsp";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (SessionUtil.tieneRol(request, Rol.ADMINISTRADOR)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        mostrarFormulario(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String correo = request.getParameter("correo");
        String password = request.getParameter("password");

        try {
            UsuarioResponseDTO usuario = authService.login(correo, password, Rol.ADMINISTRADOR);
            // Se cambia el id de sesion al autenticar (no solo se crea si
            // no existia) para que un id de sesion fijado de antemano por
            // un atacante (session fixation) quede invalidado antes de
            // que la sesion tenga privilegios de administrador.
            HttpSession sesion = request.getSession(true);
            request.changeSessionId();
            sesion.setAttribute(Constantes.SESSION_USUARIO, usuario);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } catch (AppException e) {
            logger.warn("Login de administrador rechazado: {}", e.getMessage(), e.getCause());
            mostrarFormulario(request, response, e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al iniciar sesion", e);
            mostrarFormulario(request, response, "Ocurrio un error inesperado. Intenta de nuevo.");
        }
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }
}
