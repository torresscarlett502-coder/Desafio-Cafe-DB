package sv.udb.cafedonbosco.dto.response;

import sv.udb.cafedonbosco.util.FormatoUtil;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponseDTO {

    private int productosDisponibles;
    private BigDecimal ventasHoyTotal;
    private int ventasHoyCantidad;
    private BigDecimal totalVentasMes;
    private BigDecimal ingresosTotales;
    private int pedidosRecibidos;
    private int pedidosEnPreparacion;
    private int pedidosListos;
    private List<VentaResponseDTO> ventasRecientes;
    private List<ProductoAdminResponseDTO> productosStockBajo;

    public DashboardResponseDTO() {
    }

    public int getProductosDisponibles() {
        return productosDisponibles;
    }

    public void setProductosDisponibles(int productosDisponibles) {
        this.productosDisponibles = productosDisponibles;
    }

    public BigDecimal getVentasHoyTotal() {
        return ventasHoyTotal;
    }

    public void setVentasHoyTotal(BigDecimal ventasHoyTotal) {
        this.ventasHoyTotal = ventasHoyTotal;
    }

    public int getVentasHoyCantidad() {
        return ventasHoyCantidad;
    }

    public void setVentasHoyCantidad(int ventasHoyCantidad) {
        this.ventasHoyCantidad = ventasHoyCantidad;
    }

    public BigDecimal getTotalVentasMes() {
        return totalVentasMes;
    }

    public void setTotalVentasMes(BigDecimal totalVentasMes) {
        this.totalVentasMes = totalVentasMes;
    }

    public String getVentasHoyTotalFormateado() {
        return FormatoUtil.moneda(ventasHoyTotal);
    }

    public String getTotalVentasMesFormateado() {
        return FormatoUtil.moneda(totalVentasMes);
    }

    public BigDecimal getIngresosTotales() {
        return ingresosTotales;
    }

    public void setIngresosTotales(BigDecimal ingresosTotales) {
        this.ingresosTotales = ingresosTotales;
    }

    public String getIngresosTotalesFormateado() {
        return FormatoUtil.moneda(ingresosTotales);
    }

    public int getPedidosRecibidos() {
        return pedidosRecibidos;
    }

    public void setPedidosRecibidos(int pedidosRecibidos) {
        this.pedidosRecibidos = pedidosRecibidos;
    }

    public int getPedidosEnPreparacion() {
        return pedidosEnPreparacion;
    }

    public void setPedidosEnPreparacion(int pedidosEnPreparacion) {
        this.pedidosEnPreparacion = pedidosEnPreparacion;
    }

    public int getPedidosListos() {
        return pedidosListos;
    }

    public void setPedidosListos(int pedidosListos) {
        this.pedidosListos = pedidosListos;
    }

    public List<VentaResponseDTO> getVentasRecientes() {
        return ventasRecientes;
    }

    public void setVentasRecientes(List<VentaResponseDTO> ventasRecientes) {
        this.ventasRecientes = ventasRecientes;
    }

    public List<ProductoAdminResponseDTO> getProductosStockBajo() {
        return productosStockBajo;
    }

    public void setProductosStockBajo(List<ProductoAdminResponseDTO> productosStockBajo) {
        this.productosStockBajo = productosStockBajo;
    }
}
