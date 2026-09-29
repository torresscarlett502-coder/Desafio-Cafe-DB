package sv.udb.cafedonbosco.model;

import java.math.BigDecimal;

public class DetalleCompra {

    private Integer id;
    private Integer compraId;
    private Integer productoId;
    private Integer cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal subtotal;

    public DetalleCompra() {
    }

    public DetalleCompra(
            Integer productoId,
            Integer cantidad,
            BigDecimal costoUnitario,
            BigDecimal subtotal
    ) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.subtotal = subtotal;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCompraId() {
        return compraId;
    }

    public void setCompraId(Integer compraId) {
        this.compraId = compraId;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
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
}
