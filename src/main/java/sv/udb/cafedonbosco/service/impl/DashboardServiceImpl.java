package sv.udb.cafedonbosco.service.impl;

import sv.udb.cafedonbosco.dao.ProductoDAO;
import sv.udb.cafedonbosco.dao.VentaDAO;
import sv.udb.cafedonbosco.dao.impl.ProductoDAOImpl;
import sv.udb.cafedonbosco.dao.impl.VentaDAOImpl;
import sv.udb.cafedonbosco.dto.response.DashboardResponseDTO;
import sv.udb.cafedonbosco.dto.response.ProductoAdminResponseDTO;
import sv.udb.cafedonbosco.model.EstadoVenta;
import sv.udb.cafedonbosco.service.DashboardService;
import sv.udb.cafedonbosco.service.ProductoService;
import sv.udb.cafedonbosco.service.VentaService;

import java.util.ArrayList;
import java.util.List;

public class DashboardServiceImpl implements DashboardService {

    private final VentaDAO ventaDAO;
    private final ProductoDAO productoDAO;
    private final ProductoService productoService;
    private final VentaService ventaService;

    public DashboardServiceImpl() {
        this.ventaDAO = new VentaDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
        this.productoService = new ProductoServiceImpl();
        this.ventaService = new VentaServiceImpl();
    }

    @Override
    public DashboardResponseDTO obtenerResumen() {
        DashboardResponseDTO dto = new DashboardResponseDTO();
        dto.setProductosDisponibles(productoDAO.listarActivos().size());
        dto.setVentasHoyTotal(ventaDAO.sumarTotalDelDia());
        dto.setVentasHoyCantidad(ventaDAO.contarVentasDelDia());
        dto.setTotalVentasMes(ventaDAO.sumarTotalDelMes());
        dto.setIngresosTotales(ventaDAO.sumarIngresosCobrados());
        dto.setPedidosRecibidos(ventaDAO.contarPorEstado(EstadoVenta.RECIBIDO));
        dto.setPedidosEnPreparacion(ventaDAO.contarPorEstado(EstadoVenta.EN_PREPARACION));
        dto.setPedidosListos(ventaDAO.contarPorEstado(EstadoVenta.LISTO));

        dto.setVentasRecientes(ventaService.listarHistorial(null, 5));

        List<ProductoAdminResponseDTO> stockBajo = new ArrayList<>();
        for (ProductoAdminResponseDTO producto : productoService.listarAdmin()) {
            if (Boolean.TRUE.equals(producto.getActivo())
                    && producto.getStock() != null
                    && producto.getStockMinimo() != null
                    && producto.getStock() <= producto.getStockMinimo()) {
                stockBajo.add(producto);
            }
        }
        dto.setProductosStockBajo(stockBajo);

        return dto;
    }
}
