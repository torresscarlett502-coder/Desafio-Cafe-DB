package sv.udb.cafedonbosco.model;

import java.math.BigDecimal;

/** Una opcion concreta dentro de un GrupoOpcion (ej. "Leche de almendra", +$0.50). */
public class Opcion {

    private Integer id;
    private Integer grupoId;
    private String nombre;
    private BigDecimal precioAdicional;
    private Boolean activo;

    public Opcion() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Integer grupoId) {
        this.grupoId = grupoId;
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
