package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.EstadoPagoRequestDTO;
import sv.udb.cafedonbosco.dto.request.EstadoVentaRequestDTO;
import sv.udb.cafedonbosco.dto.request.VentaFiltroDTO;
import sv.udb.cafedonbosco.dto.request.VentaPresencialRequestDTO;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.dto.response.VentaResponseDTO;
import sv.udb.cafedonbosco.exception.AccesoDenegadoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.EstadoPago;
import sv.udb.cafedonbosco.model.EstadoVenta;
import sv.udb.cafedonbosco.model.TipoVenta;
import sv.udb.cafedonbosco.service.VentaService;
import sv.udb.cafedonbosco.service.impl.VentaServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Gestion administrativa de ventas/pedidos. Protegido por RolAdminFilter
 * (/api/admin/*):
 *   GET    /api/admin/ventas            historial, con filtros opcionales
 *   GET    /api/admin/ventas/{id}       detalle de una venta
 *   POST   /api/admin/ventas            registra una venta presencial (POS)
 *   POST   /api/admin/ventas/{id}/cancelar   cancela y devuelve el stock
 *   PATCH  /api/admin/ventas/{id}/estado     cambia el estado del pedido
 *   PATCH  /api/admin/ventas/{id}/pago       cambia el estado del pago
 */
@WebServlet(name = "VentaAdminServlet", urlPatterns = {"/api/admin/ventas", "/api/admin/ventas/*"})
public class VentaAdminServlet extends BaseServlet {

    private final VentaService ventaService = new VentaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length == 0) {
                if (hayFiltrosAvanzados(request)) {
                    JsonUtil.exito(response, HttpServletResponse.SC_OK, "Historial obtenido",
                            ventaService.listarFiltrado(construirFiltro(request)));
                } else {
                    String tipoParametro = request.getParameter("tipo");
                    TipoVenta tipoVenta = null;
                    if (tipoParametro != null && !tipoParametro.isBlank()) {
                        try {
                            tipoVenta = TipoVenta.valueOf(tipoParametro.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new ValidacionException("El tipo de venta debe ser PRESENCIAL o WEB.");
                        }
                    }
                    JsonUtil.exito(response, HttpServletResponse.SC_OK, "Historial obtenido",
                            ventaService.listarHistorial(tipoVenta, parametroLimite(request)));
                }
                return;
            }

            int id = Integer.parseInt(segmentos[0]);
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Venta obtenida", ventaService.obtenerPorId(id));
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id de la venta no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UsuarioResponseDTO administrador = SessionUtil.obtenerUsuarioAutenticado(request);
            if (administrador == null) {
                throw new AccesoDenegadoException("Debes iniciar sesion como administrador.");
            }
            String[] segmentos = segmentosDePath(request);

            if (segmentos.length == 2 && "cancelar".equals(segmentos[1])) {
                int id = Integer.parseInt(segmentos[0]);
                VentaResponseDTO venta = ventaService.cancelar(id, administrador.getId());
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Venta cancelada correctamente", venta);
                return;
            }

            if (segmentos.length > 0) {
                throw new ValidacionException("Ruta no reconocida.");
            }

            VentaPresencialRequestDTO datos = JsonUtil.leerCuerpo(request, VentaPresencialRequestDTO.class);
            VentaResponseDTO venta = ventaService.registrarVentaPresencial(datos, administrador.getId());
            JsonUtil.exito(response, HttpServletResponse.SC_CREATED, "Venta registrada correctamente", venta);
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id de la venta no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UsuarioResponseDTO administrador = SessionUtil.obtenerUsuarioAutenticado(request);
            if (administrador == null) {
                throw new AccesoDenegadoException("Debes iniciar sesion como administrador.");
            }
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length != 2) {
                throw new ValidacionException("Ruta no reconocida.");
            }
            int id = Integer.parseInt(segmentos[0]);

            if ("estado".equals(segmentos[1])) {
                EstadoVentaRequestDTO datos = JsonUtil.leerCuerpo(request, EstadoVentaRequestDTO.class);
                EstadoVenta nuevoEstado = parsearEstadoVenta(datos != null ? datos.getEstado() : null);
                VentaResponseDTO venta = ventaService.cambiarEstado(id, nuevoEstado, administrador.getId());
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Estado de la venta actualizado", venta);
                return;
            }

            if ("pago".equals(segmentos[1])) {
                EstadoPagoRequestDTO datos = JsonUtil.leerCuerpo(request, EstadoPagoRequestDTO.class);
                EstadoPago nuevoEstadoPago = parsearEstadoPago(datos != null ? datos.getEstadoPago() : null);
                VentaResponseDTO venta = ventaService.cambiarEstadoPago(id, nuevoEstadoPago, administrador.getId());
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Estado de pago actualizado", venta);
                return;
            }

            throw new ValidacionException("Ruta no reconocida.");
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id de la venta no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    private EstadoVenta parsearEstadoVenta(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("Debes indicar el nuevo estado del pedido.");
        }
        try {
            return EstadoVenta.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Estado de pedido no valido: " + valor);
        }
    }

    private EstadoPago parsearEstadoPago(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("Debes indicar el nuevo estado del pago.");
        }
        try {
            return EstadoPago.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Estado de pago no valido: " + valor);
        }
    }

    private boolean hayFiltrosAvanzados(HttpServletRequest request) {
        return valorPresente(request, "estado") || valorPresente(request, "estadoPago")
                || valorPresente(request, "fechaDesde") || valorPresente(request, "fechaHasta")
                || valorPresente(request, "cliente") || valorPresente(request, "numeroVenta")
                || valorPresente(request, "metodoPago") || valorPresente(request, "page")
                || valorPresente(request, "size");
    }

    private boolean valorPresente(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor != null && !valor.isBlank();
    }

    private VentaFiltroDTO construirFiltro(HttpServletRequest request) {
        VentaFiltroDTO filtro = new VentaFiltroDTO();

        String tipoParametro = request.getParameter("tipo");
        if (tipoParametro != null && !tipoParametro.isBlank()) {
            try {
                filtro.setTipoVenta(TipoVenta.valueOf(tipoParametro.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ValidacionException("El tipo de venta debe ser PRESENCIAL o WEB.");
            }
        }
        String estadoParametro = request.getParameter("estado");
        if (estadoParametro != null && !estadoParametro.isBlank()) {
            filtro.setEstado(parsearEstadoVenta(estadoParametro));
        }
        String estadoPagoParametro = request.getParameter("estadoPago");
        if (estadoPagoParametro != null && !estadoPagoParametro.isBlank()) {
            filtro.setEstadoPago(parsearEstadoPago(estadoPagoParametro));
        }
        filtro.setFechaDesde(parametroFecha(request, "fechaDesde"));
        filtro.setFechaHasta(parametroFecha(request, "fechaHasta"));
        filtro.setCliente(request.getParameter("cliente"));
        String numeroVenta = request.getParameter("numeroVenta");
        if (numeroVenta != null && !numeroVenta.isBlank()) {
            try {
                filtro.setNumeroVenta(Integer.parseInt(numeroVenta.trim()));
            } catch (NumberFormatException e) {
                throw new ValidacionException("El numero de venta debe ser numerico.");
            }
        }
        filtro.setMetodoPago(request.getParameter("metodoPago"));

        String page = request.getParameter("page");
        if (page != null && !page.isBlank()) {
            try {
                filtro.setPage(Integer.parseInt(page.trim()));
            } catch (NumberFormatException e) {
                throw new ValidacionException("El parametro page debe ser numerico.");
            }
        }
        String size = request.getParameter("size");
        if (size != null && !size.isBlank()) {
            try {
                filtro.setSize(Integer.parseInt(size.trim()));
            } catch (NumberFormatException e) {
                throw new ValidacionException("El parametro size debe ser numerico.");
            }
        }
        return filtro;
    }

    private LocalDate parametroFecha(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new ValidacionException("El parametro " + nombre + " debe tener formato AAAA-MM-DD.");
        }
    }

    private int parametroLimite(HttpServletRequest request) {
        String valor = request.getParameter("limite");
        if (valor == null || valor.isBlank()) {
            return 50;
        }
        try {
            return Math.max(1, Integer.parseInt(valor));
        } catch (NumberFormatException e) {
            return 50;
        }
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
