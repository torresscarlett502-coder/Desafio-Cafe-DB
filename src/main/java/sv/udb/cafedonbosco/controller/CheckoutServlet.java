package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.CheckoutRequestDTO;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.dto.response.VentaResponseDTO;
import sv.udb.cafedonbosco.model.Carrito;
import sv.udb.cafedonbosco.service.VentaService;
import sv.udb.cafedonbosco.service.impl.VentaServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

/**
 * Procesa la compra del consumidor: toma el carrito de la sesion (nunca
 * del cuerpo de la solicitud), revalida todo en el servidor y registra
 * la venta WEB. El consumidor puede ser invitado; si tiene sesion
 * iniciada, la venta queda asociada a su cuenta para "Mis pedidos".
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = "/api/checkout")
public class CheckoutServlet extends BaseServlet {

    private final VentaService ventaService = new VentaServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            CheckoutRequestDTO datos = JsonUtil.leerCuerpo(request, CheckoutRequestDTO.class);
            Carrito carrito = SessionUtil.obtenerOCrearCarrito(request);
            UsuarioResponseDTO usuario = SessionUtil.obtenerUsuarioAutenticado(request);
            Integer usuarioId = usuario != null ? usuario.getId() : null;

            VentaResponseDTO venta = ventaService.procesarCheckoutWeb(carrito, datos, usuarioId, datos.getIdempotencyKey());
            JsonUtil.exito(response, HttpServletResponse.SC_CREATED, "Compra procesada correctamente", venta);
        } catch (Exception e) {
            manejarError(response, e);
        }
    }
}
