package sv.udb.cafedonbosco.dto.response;

import sv.udb.cafedonbosco.model.CarritoItem;
import sv.udb.cafedonbosco.util.FormatoUtil;

import java.math.BigDecimal;
import java.util.List;

public class CarritoResponseDTO {

    private List<CarritoItem> items;
    private int cantidadUnidades;
    private BigDecimal subtotal;
    private BigDecimal envio;
    private BigDecimal total;

    public CarritoResponseDTO() {
    }

    public CarritoResponseDTO(List<CarritoItem> items, int cantidadUnidades, BigDecimal subtotal, BigDecimal envio, BigDecimal total) {
        this.items = items;
        this.cantidadUnidades = cantidadUnidades;
        this.subtotal = subtotal;
        this.envio = envio;
        this.total = total;
    }

    public List<CarritoItem> getItems() {
        return items;
    }

    public void setItems(List<CarritoItem> items) {
        this.items = items;
    }

    public int getCantidadUnidades() {
        return cantidadUnidades;
    }

    public void setCantidadUnidades(int cantidadUnidades) {
        this.cantidadUnidades = cantidadUnidades;
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

    public String getSubtotalFormateado() {
        return FormatoUtil.moneda(subtotal);
    }

    public String getEnvioFormateado() {
        return FormatoUtil.moneda(envio);
    }

    public String getTotalFormateado() {
        return FormatoUtil.moneda(total);
    }
}
