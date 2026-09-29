package sv.udb.cafedonbosco.dto.request;

import sv.udb.cafedonbosco.model.EstadoPago;
import sv.udb.cafedonbosco.model.EstadoVenta;
import sv.udb.cafedonbosco.model.TipoVenta;

import java.time.LocalDate;

/**
 * Filtros del historial administrativo de ventas. page/size sustituyen
 * al antiguo "limite" sin tope: size siempre se acota a un maximo
 * razonable (ver Constantes.TAMANO_PAGINA_MAXIMO) para no permitir un
 * volcado completo de la tabla de un solo golpe.
 */
public class VentaFiltroDTO {

    private TipoVenta tipoVenta;
    private EstadoVenta estado;
    private EstadoPago estadoPago;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private String cliente;
    private Integer numeroVenta;
    private String metodoPago;
    private int page = 0;
    private int size = 20;

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

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public Integer getNumeroVenta() {
        return numeroVenta;
    }

    public void setNumeroVenta(Integer numeroVenta) {
        this.numeroVenta = numeroVenta;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(page, 0);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
