package sv.udb.cafedonbosco.model;

public class Inventario {

    private Integer id;
    private Integer productoId;
    private Integer cantidad;
    private Integer stockMinimo;

    public Inventario() {
    }

    public Inventario(
            Integer id,
            Integer productoId,
            Integer cantidad,
            Integer stockMinimo
    ) {
        this.id = id;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
