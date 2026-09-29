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
import sv.udb.cafedonbosco.util.Constantes;

import java.io.IOException;

/**
 * Habilita CORS para la API solo para los origenes de desarrollo listados
 * en Constantes.ORIGENES_CORS_PERMITIDOS. Antes se reflejaba cualquier
 * Origin de la solicitud junto con Access-Control-Allow-Credentials en
 * true, lo que en la practica anulaba la proteccion de origen del
 * navegador: cualquier sitio malicioso podia hacer solicitudes
 * autenticadas a la API usando la cookie de sesion de la victima. Ahora
 * el encabezado solo se agrega cuando el origen esta en la lista
 * permitida; para cualquier otro origen no se agrega ningun encabezado
 * CORS y el navegador bloquea la respuesta.
 */
@WebFilter("/api/*")
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String origen = httpRequest.getHeader("Origin");
        if (origen != null && Constantes.ORIGENES_CORS_PERMITIDOS.contains(origen)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origen);
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Vary", "Origin");
        }
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
