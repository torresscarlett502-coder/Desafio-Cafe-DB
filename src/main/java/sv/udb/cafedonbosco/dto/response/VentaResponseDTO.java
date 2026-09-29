package sv.udb.cafedonbosco.dto.response;

import sv.udb.cafedonbosco.model.EstadoPago;
import sv.udb.cafedonbosco.model.EstadoVenta;
import sv.udb.cafedonbosco.model.TipoVenta;
import sv.udb.cafedonbosco.util.FormatoUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Representa una venta ya registrada, tanto para la confirmacion de compra
 * del consumidor como para el historial del administrador. El
 * tokenTicket solo debe entregarse a quien realizo la compra: el
 * historial administrativo lo omite (queda en null) porque ya autentica
 * al administrador por sesion.
 */
public class VentaResponseDTO {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es"));
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("hh:mm a", new Locale("es"));

    private Integer id;
    private TipoVenta tipoVenta;
    private EstadoVenta estado;
    private BigDecimal subtotal;
    private BigDecimal envio;
    private BigDecimal total;
    private String metodoPago;
    private EstadoPago estadoPago;
    private String tipoEntrega;
    private String nombreCliente;
    private String correoCliente;
    private String telefonoCliente;
    private String direccionCliente;
    private String notas;
    private LocalDateTime fecha;
    private LocalDateTime fechaInicioPreparacion;
    private LocalDateTime fechaEstimadaListo;
    private LocalDateTime fechaListo;
    private LocalDateTime fechaEntregado;
    private String tokenTicket;
    private List<DetalleVentaResponseDTO> detalles;

    public VentaResponseDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TipoVenta getTipoVenta() {
        return tipoVenta;
    }

    public void setTipoVenta(TipoVenta tipoVenta) {
        this.tipoVenta = tipoVenta;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoVenta estado) {
        this.estado = estado;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getEnvio() {
        return envio;
    }

    public void setEnvio(BigDecimal envio) {
        this.envio = envio;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(String tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public String getDireccionCliente() {
        return direccionCliente;
    }

    public void setDireccionCliente(String direccionCliente) {
        this.direccionCliente = direccionCliente;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public LocalDateTime getFechaInicioPreparacion() {
        return fechaInicioPreparacion;
    }

    public void setFechaInicioPreparacion(LocalDateTime fechaInicioPreparacion) {
        this.fechaInicioPreparacion = fechaInicioPreparacion;
    }

    public LocalDateTime getFechaEstimadaListo() {
        return fechaEstimadaListo;
    }

    public void setFechaEstimadaListo(LocalDateTime fechaEstimadaListo) {
        this.fechaEstimadaListo = fechaEstimadaListo;
    }

    public LocalDateTime getFechaListo() {
        return fechaListo;
    }

    public void setFechaListo(LocalDateTime fechaListo) {
        this.fechaListo = fechaListo;
    }

    public LocalDateTime getFechaEntregado() {
        return fechaEntregado;
    }

    public void setFechaEntregado(LocalDateTime fechaEntregado) {
        this.fechaEntregado = fechaEntregado;
    }

    /**
     * Fecha/hora formateadas para las vistas JSP: JSTL fmt:formatDate solo
     * acepta java.util.Date, no LocalDateTime, asi que la vista usa estos
     * getters en vez de intentar formatear el campo crudo.
     */
    public String getFechaFormateada() {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "";
    }

    public String getHoraFormateada() {
        return fecha != null ? fecha.format(FORMATO_HORA) : "";
    }

    public String getSubtotalFormateado() {
        return FormatoUtil.moneda(subtotal);
    }

    public String getEnvioFormateado() {
        return FormatoUtil.moneda(envio);
    }

    public String getTotalFormateado() {
        return FormatoUtil.moneda(total);
    }

    /** Resumen tipo "2x Croissant, 1x Cafe Latte" para listados administrativos. */
    public String getDescripcionItems() {
        if (detalles == null || detalles.isEmpty()) {
            return "";
        }
        StringBuilder resumen = new StringBuilder();
        for (int i = 0; i < detalles.size(); i++) {
            DetalleVentaResponseDTO detalle = detalles.get(i);
            if (i > 0) {
                resumen.append(", ");
            }
            resumen.append(detalle.getCantidad()).append("x ").append(detalle.getNombreProducto());
        }
        return resumen.toString();
    }

    public String getTokenTicket() {
        return tokenTicket;
    }

    public void setTokenTicket(String tokenTicket) {
        this.tokenTicket = tokenTicket;
    }

    public List<DetalleVentaResponseDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaResponseDTO> detalles) {
        this.detalles = detalles;
    }
}
