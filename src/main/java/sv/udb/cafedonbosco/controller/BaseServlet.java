package sv.udb.cafedonbosco.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sv.udb.cafedonbosco.exception.AppException;
import sv.udb.cafedonbosco.util.JsonUtil;

import java.io.IOException;

/**
 * Centraliza el manejo de errores de todos los servlets: una AppException
 * ya trae su codigo HTTP y un mensaje seguro para el cliente; cualquier
 * otro error se registra en el log del servidor y se responde con un
 * mensaje generico, sin exponer trazas ni detalles internos.
 */
public abstract class BaseServlet extends HttpServlet {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * HttpServlet no reconoce PATCH de forma nativa (solo GET/POST/PUT/
     * DELETE/HEAD/OPTIONS/TRACE en service()), asi que se intercepta aqui
     * para que cualquier servlet pueda sobreescribir doPatch igual que ya
     * hace con doGet/doPost/doPut.
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(request.getMethod())) {
            doPatch(request, response);
        } else {
            super.service(request, response);
        }
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    protected void manejarError(HttpServletResponse response, Exception excepcion) throws IOException {
        if (excepcion instanceof AppException appException) {
            if (appException.getCause() != null) {
                logger.warn("AppException con causa: {}", appException.getMessage(), appException.getCause());
            }
            JsonUtil.error(response, appException.getCodigoHttp(), appException.getMessage());
            return;
        }
        logger.error("Error inesperado en el servlet", excepcion);
        JsonUtil.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Ocurrio un error inesperado. Intenta de nuevo mas tarde.");
    }
}
