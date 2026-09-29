package sv.udb.cafedonbosco.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.model.Rol;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 * Protege todas las rutas /api/admin/*: exige una sesion activa cuyo
 * usuario tenga rol ADMINISTRADOR. El login del consumidor y el del
 * administrador comparten el mismo AuthServlet; lo que distingue el
 * acceso es este filtro, no un servlet de login separado.
 */
@WebFilter("/api/admin/*")
public class RolAdminFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        if (!SessionUtil.tieneRol(httpRequest, Rol.ADMINISTRADOR)) {
            JsonUtil.error(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesion como administrador.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
