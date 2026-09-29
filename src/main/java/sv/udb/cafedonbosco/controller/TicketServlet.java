package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.response.VentaResponseDTO;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.service.TicketService;
import sv.udb.cafedonbosco.service.impl.TicketServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.TicketPdfGenerator;

import java.io.IOException;
import java.util.Map;

/**
 * GET  /api/tickets/{token}[/pdf|/whatsapp-link]  publico pero exige el
 * token aleatorio de la venta (nunca el id incremental), para que nadie
 * pueda ver el ticket de otro comprador cambiando un numero en la URL.
 * POST /api/tickets/{token}/enviar-email                publico, idem.
 * Las mismas rutas bajo /api/admin/tickets/{id}/... estan protegidas por
 * RolAdminFilter y aceptan el id porque el administrador ya esta
 * autenticado.
 */
@WebServlet(name = "TicketServlet", urlPatterns = {"/api/tickets/*", "/api/admin/tickets/*"})
public class TicketServlet extends BaseServlet {

    private final TicketService ticketService = new TicketServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length == 0) {
                throw new ValidacionException("Debes indicar el ticket a consultar en la URL.");
            }
            VentaResponseDTO ticket = resolverTicket(request, segmentos[0]);
            String accion = segmentos.length == 2 ? segmentos[1] : null;

            if ("pdf".equals(accion)) {
                enviarPdf(response, ticket);
                return;
            }
            if ("whatsapp-link".equals(accion)) {
                String telefono = request.getParameter("telefono");
                String enlace = ticketService.generarEnlaceWhatsApp(ticket, telefono);
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Enlace de WhatsApp generado",
                        Map.of("whatsappUrl", enlace));
                return;
            }
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Ticket obtenido", ticket);
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id de la venta no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length != 2 || !"enviar-email".equals(segmentos[1])) {
                throw new ValidacionException("Ruta no reconocida.");
            }
            VentaResponseDTO ticket = resolverTicket(request, segmentos[0]);

            @SuppressWarnings("unchecked")
            Map<String, Object> cuerpo = JsonUtil.leerCuerpo(request, Map.class);
            Object valorCorreo = cuerpo != null ? cuerpo.get("email") : null;
            String correo = valorCorreo != null ? valorCorreo.toString() : null;

            ticketService.enviarPorCorreo(ticket, correo);
            JsonUtil.exito(response, HttpServletResponse.SC_OK,
                    "El ticket se esta enviando a tu correo.", null);
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id de la venta no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    private VentaResponseDTO resolverTicket(HttpServletRequest request, String valor) {
        boolean esAdmin = request.getServletPath().startsWith("/api/admin");
        return esAdmin ? ticketService.obtenerPorIdAdmin(Integer.parseInt(valor)) : ticketService.obtenerPorToken(valor);
    }

    private void enviarPdf(HttpServletResponse response, VentaResponseDTO ticket) throws IOException {
        byte[] pdf = TicketPdfGenerator.generar(ticket);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"ticket-" + ticket.getId() + ".pdf\"");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
        response.getOutputStream().flush();
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
