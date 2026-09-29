package sv.udb.cafedonbosco.dto.response;

import sv.udb.cafedonbosco.util.FormatoUtil;

import java.math.BigDecimal;

public class OpcionSeleccionadaResponseDTO {

    private Integer opcionId;
    private String nombreGrupo;
    private String nombreOpcion;
    private BigDecimal precioAdicional;

    public OpcionSeleccionadaResponseDTO() {
    }

    public OpcionSeleccionadaResponseDTO(Integer opcionId, String nombreGrupo, String nombreOpcion, BigDecimal precioAdicional) {
        this.opcionId = opcionId;
        this.nombreGrupo = nombreGrupo;
        this.nombreOpcion = nombreOpcion;
        this.precioAdicional = precioAdicional;
    }

    public Integer getOpcionId() {
        return opcionId;
    }

    public void setOpcionId(Integer opcionId) {
        this.opcionId = opcionId;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public void setNombreGrupo(String nombreGrupo) {
        this.nombreGrupo = nombreGrupo;
    }

    public String getNombreOpcion() {
        return nombreOpcion;
    }

    public void setNombreOpcion(String nombreOpcion) {
        this.nombreOpcion = nombreOpcion;
    }

    public BigDecimal getPrecioAdicional() {
        return precioAdicional;
    }

    public void setPrecioAdicional(BigDecimal precioAdicional) {
        this.precioAdicional = precioAdicional;
    }

    public String getPrecioAdicionalFormateado() {
        return FormatoUtil.moneda(precioAdicional);
    }
}
