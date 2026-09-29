package sv.udb.cafedonbosco.dto.request;

public class InventarioAjusteRequestDTO {

    private Integer productoId;
    private Integer cantidad;
    private Integer stockMinimo;

    public InventarioAjusteRequestDTO() {
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

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
}
