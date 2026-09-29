package sv.udb.cafedonbosco.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo unificado de operacion de venta: cubre tanto la venta presencial
 * (POS del administrador) como el pedido web del consumidor. TipoVenta
 * distingue el origen y evita duplicar entidades e inventario.
 * <p>
 * `estado` es el ciclo del PEDIDO (RECIBIDO..ENTREGADO/CANCELADO) y
 * `estadoPago` es el ciclo del PAGO (PENDIENTE/APROBADO/RECHAZADO): son
 * conceptos independientes, una venta puede estar EN_PREPARACION con el
 * pago todavia PENDIENTE (pago contra entrega, por ejemplo).
 */
public class Venta {

    private Integer id;
    private Integer usuarioId;
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
    private String tokenTicket;
    private String idempotencyKey;
    private LocalDateTime fecha;
    private LocalDateTime fechaInicioPreparacion;
    private LocalDateTime fechaEstimadaListo;
    private LocalDateTime fechaListo;
    private LocalDateTime fechaEntregado;
    private LocalDateTime actualizadoEn;
    private List<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
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

    public String getTokenTicket() {
        return tokenTicket;
    }

    public void setTokenTicket(String tokenTicket) {
        this.tokenTicket = tokenTicket;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }
}
