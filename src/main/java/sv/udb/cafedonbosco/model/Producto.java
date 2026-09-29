package sv.udb.cafedonbosco.model;

import java.math.BigDecimal;

public class Producto {

    private Integer id;
    private Integer categoriaId;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String imagen;
    private Integer tiempoPreparacionMinutos;
    private Boolean activo;

    public Producto() {
    }

    public Producto(
            Integer id,
            Integer categoriaId,
            String nombre,
            String descripcion,
            BigDecimal precio,
            String imagen,
            Integer tiempoPreparacionMinutos,
            Boolean activo
    ) {
        this.id = id;
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagen = imagen;
        this.tiempoPreparacionMinutos = tiempoPreparacionMinutos;
        this.activo = activo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Integer getTiempoPreparacionMinutos() {
        return tiempoPreparacionMinutos;
    }

    public void setTiempoPreparacionMinutos(Integer tiempoPreparacionMinutos) {
        this.tiempoPreparacionMinutos = tiempoPreparacionMinutos;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
