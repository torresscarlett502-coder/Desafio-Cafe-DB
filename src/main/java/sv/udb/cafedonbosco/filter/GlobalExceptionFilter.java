package sv.udb.cafedonbosco.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sv.udb.cafedonbosco.exception.AppException;
import sv.udb.cafedonbosco.util.JsonUtil;

import java.io.IOException;

/**
 * Red de seguridad final para /api/*: cada servlet ya envuelve su propia
 * logica en try/catch y responde con BaseServlet.manejarError, asi que en
 * condiciones normales este filtro nunca deberia tener nada que hacer.
 * Existe para el caso en que algo se escape de ese manejo (un Throwable
 * que no sea Exception, como un Error, o un bug futuro que olvide el
 * try/catch): sin esto, ese error llegaria sin filtrar hasta la pagina de
 * error por defecto de Tomcat, que puede incluir la traza completa del
 * servidor en la respuesta.
 */
@WebFilter("/api/*")
public class GlobalExceptionFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable error) {
            manejarError((HttpServletResponse) response, error);
        }
    }

    private void manejarError(HttpServletResponse response, Throwable error) throws IOException {
        if (response.isCommitted()) {
            // El servlet ya empezo a escribir su propia respuesta antes de
            // fallar; ya no hay forma segura de reemplazarla.
            LOG.error("Error no manejado despues de comprometer la respuesta", error);
            return;
        }
        if (error instanceof AppException appException) {
            LOG.warn("AppException sin capturar en el servlet: {}", appException.getMessage(), appException.getCause());
            JsonUtil.error(response, appException.getCodigoHttp(), appException.getMessage());
            return;
        }
        LOG.error("Error no manejado en la capa de servlets", error);
        JsonUtil.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Ocurrio un error inesperado. Intenta de nuevo mas tarde.");
    }

    @Override
    public void destroy() {
    }
}
