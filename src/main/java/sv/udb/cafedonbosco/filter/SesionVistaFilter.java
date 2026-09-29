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
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 * Protege todas las pantallas JSP del panel de administrador
 * (/admin/*): exige una sesion activa con rol ADMINISTRADOR, igual que
 * RolAdminFilter hace para la API bajo /api/admin/*.
 * <p>
 * El consumidor nunca inicia sesion (entra directo desde el portal), asi
 * que las rutas de la tienda (/tienda/*) no pasan por este filtro.
 */
@WebFilter("/admin/*")
public class SesionVistaFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!SessionUtil.tieneRol(httpRequest, Rol.ADMINISTRADOR)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
