package sv.udb.cafedonbosco.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * proveedorNombre es una fotografia del nombre del proveedor al momento
 * de la compra (igual que detalle_venta.nombre_producto): si el
 * proveedor cambia de nombre despues, el historial de compras no debe
 * cambiar retroactivamente.
 */
public class Compra {

    private Integer id;
    private Integer proveedorId;
    private String proveedorNombre;
    private Integer usuarioId;
    private BigDecimal total;
    private LocalDateTime fecha;
    private List<DetalleCompra> detalles = new ArrayList<>();

    public Compra() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Integer proveedorId) {
        this.proveedorId = proveedorId;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public List<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = detalles;
    }
}
