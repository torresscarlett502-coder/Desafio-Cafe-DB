package sv.udb.cafedonbosco.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DetalleVenta {

    private Integer id;
    private Integer ventaId;
    private Integer productoId;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private List<OpcionSeleccionada> opciones = new ArrayList<>();
    /**
     * Ids de opciones pedidas para esta linea, antes de resolverse contra
     * PersonalizacionService dentro de la transaccion. Solo se usa como
     * entrada de VentaServiceImpl.registrarConTransaccion; no se persiste
     * (lo que se guarda es "opciones", ya resuelto).
     */
    private List<Integer> opcionIdsSolicitados = new ArrayList<>();

    public DetalleVenta() {
    }

    public DetalleVenta(
            Integer productoId,
            String nombreProducto,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVentaId() {
        return ventaId;
    }

    public void setVentaId(Integer ventaId) {
        this.ventaId = ventaId;
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

    public List<OpcionSeleccionada> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionSeleccionada> opciones) {
        this.opciones = opciones != null ? opciones : new ArrayList<>();
    }

    public List<Integer> getOpcionIdsSolicitados() {
        return opcionIdsSolicitados;
    }

    public void setOpcionIdsSolicitados(List<Integer> opcionIdsSolicitados) {
        this.opcionIdsSolicitados = opcionIdsSolicitados != null ? opcionIdsSolicitados : new ArrayList<>();
    }
}
