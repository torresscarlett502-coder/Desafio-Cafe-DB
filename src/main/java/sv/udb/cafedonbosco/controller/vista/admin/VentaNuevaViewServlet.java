package sv.udb.cafedonbosco.controller.vista.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.CarritoItemRequestDTO;
import sv.udb.cafedonbosco.dto.request.VentaPresencialRequestDTO;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.dto.response.VentaResponseDTO;
import sv.udb.cafedonbosco.exception.AppException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.Carrito;
import sv.udb.cafedonbosco.model.CarritoItem;
import sv.udb.cafedonbosco.service.CarritoService;
import sv.udb.cafedonbosco.service.CategoriaService;
import sv.udb.cafedonbosco.service.ProductoService;
import sv.udb.cafedonbosco.service.VentaService;
import sv.udb.cafedonbosco.service.impl.CarritoServiceImpl;
import sv.udb.cafedonbosco.service.impl.CategoriaServiceImpl;
import sv.udb.cafedonbosco.service.impl.ProductoServiceImpl;
import sv.udb.cafedonbosco.service.impl.VentaServiceImpl;
import sv.udb.cafedonbosco.util.FechaUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Punto de venta (POS) del mostrador. Reutiliza el mismo Carrito/CarritoService
 * del consumidor, pero guardado en un atributo de sesion distinto
 * (SESSION_CARRITO_ADMIN) para no mezclarlo con una compra web. Al
 * confirmar, convierte el carrito en una venta PRESENCIAL a traves de
 * VentaService.registrarVentaPresencial, que vuelve a validar precio y
 * stock igual que el checkout del consumidor.
 */
@WebServlet(name = "AdminVentaNuevaViewServlet", urlPatterns = "/admin/venta-nueva")
public class VentaNuevaViewServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/admin/venta-nueva.jsp";

    private final ProductoService productoService = new ProductoServiceImpl();
    private final CarritoService carritoService = new CarritoServiceImpl();
    private final VentaService ventaService = new VentaServiceImpl();
    private final CategoriaService categoriaService = new CategoriaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        mostrarFormulario(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Carrito carritoAdmin = SessionUtil.obtenerOCrearCarritoAdmin(request);
        String accion = request.getParameter("accion");

        try {
            switch (accion == null ? "" : accion) {
                case "agregar" -> {
                    int productoId = Integer.parseInt(request.getParameter("productoId"));
                    carritoService.agregarProducto(carritoAdmin, productoId, 1);
                }
                case "decrementar" -> {
                    String claveLinea = request.getParameter("claveLinea");
                    CarritoItem actual = carritoAdmin.getItems().get(claveLinea);
                    if (actual != null && actual.getCantidad() > 1) {
                        carritoService.actualizarCantidad(carritoAdmin, claveLinea, actual.getCantidad() - 1);
                    } else {
                        carritoService.eliminarProducto(carritoAdmin, claveLinea);
                    }
                }
                case "eliminar" -> carritoService.eliminarProducto(carritoAdmin, request.getParameter("claveLinea"));
                case "vaciar" -> carritoService.vaciar(carritoAdmin);
                case "confirmar" -> {
                    int ventaId = confirmarVenta(request, carritoAdmin);
                    response.sendRedirect(request.getContextPath() + "/admin/ticket?id=" + ventaId + "&nueva=1");
                    return;
                }
                default -> {
                    // Sin accion reconocida: simplemente se vuelve a mostrar la pantalla.
                }
            }
            response.sendRedirect(request.getContextPath() + "/admin/venta-nueva");
        } catch (AppException e) {
            mostrarFormulario(request, response, e.getMessage());
        }
    }

    private int confirmarVenta(HttpServletRequest request, Carrito carritoAdmin) {
        if (carritoAdmin.estaVacio()) {
            throw new ValidacionException("Agrega al menos un producto antes de registrar la venta.");
        }
        UsuarioResponseDTO administrador = SessionUtil.obtenerUsuarioAutenticado(request);

        List<CarritoItemRequestDTO> items = new ArrayList<>();
        for (CarritoItem item : carritoAdmin.getItems().values()) {
            CarritoItemRequestDTO dto = new CarritoItemRequestDTO();
            dto.setProductoId(item.getProductoId());
            dto.setCantidad(item.getCantidad());
            items.add(dto);
        }

        VentaPresencialRequestDTO solicitud = new VentaPresencialRequestDTO();
        solicitud.setItems(items);
        solicitud.setMetodoPago(request.getParameter("metodoPago"));

        VentaResponseDTO venta = ventaService.registrarVentaPresencial(solicitud, administrador.getId());
        carritoService.vaciar(carritoAdmin);
        return venta.getId();
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("activo", "venta-nueva");
        request.setAttribute("usuario", SessionUtil.obtenerUsuarioAutenticado(request));
        request.setAttribute("error", error);
        request.setAttribute("productos", productoService.listarAdmin());
        request.setAttribute("categorias", categoriaService.listarActivas());
        request.setAttribute("fechaHoy", FechaUtil.obtenerFechaActualFormateada());

        Carrito carritoAdmin = SessionUtil.obtenerOCrearCarritoAdmin(request);
        request.setAttribute("carrito", carritoService.obtenerResumen(carritoAdmin));

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }
}
