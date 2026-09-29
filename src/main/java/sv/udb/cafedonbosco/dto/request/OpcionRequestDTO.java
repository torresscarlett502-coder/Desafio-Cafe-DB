package sv.udb.cafedonbosco.dto.request;

import java.math.BigDecimal;

public class OpcionRequestDTO {

    private String nombre;
    private BigDecimal precioAdicional;
    private Boolean activo;

    public OpcionRequestDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecioAdicional() {
        return precioAdicional;
    }

    public void setPrecioAdicional(BigDecimal precioAdicional) {
        this.precioAdicional = precioAdicional;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
