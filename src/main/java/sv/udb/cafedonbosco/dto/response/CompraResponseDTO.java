package sv.udb.cafedonbosco.dto.response;

import sv.udb.cafedonbosco.util.FormatoUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CompraResponseDTO {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es"));
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("hh:mm a", new Locale("es"));

    private Integer id;
    private Integer proveedorId;
    private String proveedorNombre;
    private Integer usuarioId;
    private String usuarioNombre;
    private BigDecimal total;
    private LocalDateTime fecha;
    private List<DetalleCompraResponseDTO> items;

    public CompraResponseDTO() {
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

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getTotalFormateado() {
        return FormatoUtil.moneda(total);
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getFechaFormateada() {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "";
    }

    public String getHoraFormateada() {
        return fecha != null ? fecha.format(FORMATO_HORA) : "";
    }

    public List<DetalleCompraResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<DetalleCompraResponseDTO> items) {
        this.items = items;
    }
}
