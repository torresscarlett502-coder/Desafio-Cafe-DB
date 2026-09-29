package sv.udb.cafedonbosco.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Una opcion de personalizacion ya elegida para una linea del carrito o
 * de una venta. Guarda su propia fotografia de nombre/precio (igual
 * criterio que CarritoItem con el producto): si la Opcion original
 * cambia de precio despues, una venta ya facturada no cambia.
 * Serializable porque vive dentro de CarritoItem, que a su vez vive en
 * la sesion HTTP.
 */
public class OpcionSeleccionada implements Serializable {

    private Integer opcionId;
    private String nombreGrupo;
    private String nombreOpcion;
    private BigDecimal precioAdicional;

    public OpcionSeleccionada() {
    }

    public OpcionSeleccionada(Integer opcionId, String nombreGrupo, String nombreOpcion, BigDecimal precioAdicional) {
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
}
