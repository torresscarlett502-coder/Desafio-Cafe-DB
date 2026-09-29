package sv.udb.cafedonbosco.dto.response;

import sv.udb.cafedonbosco.util.FormatoUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DetalleVentaResponseDTO {

    private Integer productoId;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private List<OpcionSeleccionadaResponseDTO> opciones = new ArrayList<>();

    public DetalleVentaResponseDTO() {
    }

    public DetalleVentaResponseDTO(Integer productoId, String nombreProducto, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getPrecioUnitarioFormateado() {
        return FormatoUtil.moneda(precioUnitario);
    }

    public String getSubtotalFormateado() {
        return FormatoUtil.moneda(subtotal);
    }

    public List<OpcionSeleccionadaResponseDTO> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionSeleccionadaResponseDTO> opciones) {
        this.opciones = opciones != null ? opciones : new ArrayList<>();
    }
}
