package sv.udb.cafedonbosco.controller.vista.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.response.VentaResponseDTO;
import sv.udb.cafedonbosco.model.EstadoVenta;
import sv.udb.cafedonbosco.service.VentaService;
import sv.udb.cafedonbosco.service.impl.VentaServiceImpl;
import sv.udb.cafedonbosco.util.FechaUtil;
import sv.udb.cafedonbosco.util.FormatoUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminHistorialVentasViewServlet", urlPatterns = "/admin/historial-ventas")
public class HistorialVentasViewServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/admin/historial-ventas.jsp";

    private final VentaService ventaService = new VentaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<VentaResponseDTO> ventas = ventaService.listarHistorial(null, 100);

        request.setAttribute("activo", "historial");
        request.setAttribute("usuario", SessionUtil.obtenerUsuarioAutenticado(request));
        request.setAttribute("ventas", ventas);
        request.setAttribute("fechaHoy", FechaUtil.obtenerFechaActualFormateada());
        calcularEstadisticasDeHoy(request, ventas);

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA);
        dispatcher.forward(request, response);
    }

    /**
     * Se calculan sobre la lista de historial ya cargada (hasta 100 ventas
     * mas recientes) en vez de pedir agregados aparte a la base de datos:
     * para el volumen de un solo mostrador, esas 100 siempre cubren el dia
     * completo, y asi la tarjeta de resumen nunca puede desalinearse con
     * la tabla que el administrador ve debajo.
     */
    private void calcularEstadisticasDeHoy(HttpServletRequest request, List<VentaResponseDTO> ventas) {
        LocalDate hoy = FechaUtil.obtenerFechaHoraActual().toLocalDate();
        BigDecimal totalHoy = BigDecimal.ZERO;
        int cantidadHoy = 0;
        int cantidadAnuladas = 0;
        int unidadesHoy = 0;
        Map<String, Integer> conteoPorMetodo = new HashMap<>();

        for (VentaResponseDTO venta : ventas) {
            if (venta.getFecha() == null || !venta.getFecha().toLocalDate().equals(hoy)) {
                continue;
            }
            if (venta.getEstado() == EstadoVenta.CANCELADO) {
                cantidadAnuladas++;
                continue;
            }
            cantidadHoy++;
            totalHoy = totalHoy.add(venta.getTotal() != null ? venta.getTotal() : BigDecimal.ZERO);
            unidadesHoy += contarUnidades(venta);
            String metodo = venta.getMetodoPago() != null ? venta.getMetodoPago() : "N/D";
            conteoPorMetodo.merge(metodo, 1, Integer::sum);
        }

        BigDecimal ticketPromedio = cantidadHoy > 0
                ? totalHoy.divide(BigDecimal.valueOf(cantidadHoy), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        double itemsPromedio = cantidadHoy > 0 ? (double) unidadesHoy / cantidadHoy : 0;

        String metodoPrincipal = "N/D";
        int maxConteo = 0;
        for (Map.Entry<String, Integer> entrada : conteoPorMetodo.entrySet()) {
            if (entrada.getValue() > maxConteo) {
                maxConteo = entrada.getValue();
                metodoPrincipal = entrada.getKey();
            }
        }
        int porcentajeMetodoPrincipal = cantidadHoy > 0 ? Math.round(maxConteo * 100f / cantidadHoy) : 0;

        request.setAttribute("ventasHoyTotalFormateado", FormatoUtil.moneda(totalHoy));
        request.setAttribute("ventasHoyCantidad", cantidadHoy);
        request.setAttribute("ventasHoyAnuladas", cantidadAnuladas);
        request.setAttribute("ticketPromedioFormateado", FormatoUtil.moneda(ticketPromedio));
        request.setAttribute("itemsPromedio", String.format("%.1f", itemsPromedio));
        request.setAttribute("metodoPrincipal", metodoPrincipal);
        request.setAttribute("porcentajeMetodoPrincipal", porcentajeMetodoPrincipal);
    }

    private int contarUnidades(VentaResponseDTO venta) {
        int total = 0;
        for (var detalle : venta.getDetalles()) {
            total += detalle.getCantidad() != null ? detalle.getCantidad() : 0;
        }
        return total;
    }
}
