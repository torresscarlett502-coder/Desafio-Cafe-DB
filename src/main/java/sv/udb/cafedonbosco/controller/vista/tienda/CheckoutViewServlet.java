package sv.udb.cafedonbosco.controller.vista.tienda;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.CheckoutRequestDTO;
import sv.udb.cafedonbosco.dto.response.VentaResponseDTO;
import sv.udb.cafedonbosco.exception.AppException;
import sv.udb.cafedonbosco.model.Carrito;
import sv.udb.cafedonbosco.service.CarritoService;
import sv.udb.cafedonbosco.service.VentaService;
import sv.udb.cafedonbosco.service.impl.CarritoServiceImpl;
import sv.udb.cafedonbosco.service.impl.VentaServiceImpl;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 * Checkout del consumidor: toma el carrito de la sesion (nunca del
 * formulario) y lo registra como venta WEB. El consumidor sigue sin
 * necesitar cuenta ni login, por eso usuarioId siempre va null aqui.
 */
@WebServlet(name = "TiendaCheckoutViewServlet", urlPatterns = "/tienda/checkout")
public class CheckoutViewServlet extends TiendaBaseServlet {

    private static final String VISTA = "/WEB-INF/views/tienda/checkout.jsp";

    private final CarritoService carritoService = new CarritoServiceImpl();
    private final VentaService ventaService = new VentaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Carrito carrito = SessionUtil.obtenerOCrearCarrito(request);
        if (carrito.estaVacio()) {
            response.sendRedirect(request.getContextPath() + "/tienda/carrito");
            return;
        }
        SessionUtil.generarNuevaClaveCheckout(request);
        mostrarFormulario(request, response, null, new CheckoutRequestDTO());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CheckoutRequestDTO datos = new CheckoutRequestDTO();
        datos.setNombreCompleto(request.getParameter("nombreCompleto"));
        datos.setCorreo(request.getParameter("correo"));
        datos.setTelefono(request.getParameter("telefono"));
        datos.setTipoEntrega(request.getParameter("tipoEntrega"));
        datos.setDireccion(request.getParameter("direccion"));
        datos.setNotas(request.getParameter("notas"));
        datos.setMetodoPago(request.getParameter("metodoPago"));

        Carrito carrito = SessionUtil.obtenerOCrearCarrito(request);
        String idempotencyKey = SessionUtil.obtenerClaveCheckout(request);
        try {
            VentaResponseDTO venta = ventaService.procesarCheckoutWeb(carrito, datos, null, idempotencyKey);
            SessionUtil.limpiarClaveCheckout(request);
            response.sendRedirect(request.getContextPath() + "/tienda/confirmacion?token=" + venta.getTokenTicket());
        } catch (AppException e) {
            mostrarFormulario(request, response, e.getMessage(), datos);
        }
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                    String error, CheckoutRequestDTO datos)
            throws ServletException, IOException {
        request.setAttribute("activo", "carrito");
        request.setAttribute("carritoUnidades", contarUnidadesCarrito(request));
        request.setAttribute("error", error);
        request.setAttribute("datos", datos);

        Carrito carrito = SessionUtil.obtenerOCrearCarrito(request);
        request.setAttribute("resumen", carritoService.obtenerResumen(carrito));

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }
}
