package sv.udb.cafedonbosco.dto.response;

import sv.udb.cafedonbosco.util.FormatoUtil;

import java.math.BigDecimal;

public class DetalleCompraResponseDTO {

    private Integer productoId;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal subtotal;

    public DetalleCompraResponseDTO() {
    }

    public DetalleCompraResponseDTO(Integer productoId, String nombreProducto, Integer cantidad,
                                     BigDecimal costoUnitario, BigDecimal subtotal) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
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

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getCostoUnitarioFormateado() {
        return FormatoUtil.moneda(costoUnitario);
    }

    public String getSubtotalFormateado() {
        return FormatoUtil.moneda(subtotal);
    }
}
