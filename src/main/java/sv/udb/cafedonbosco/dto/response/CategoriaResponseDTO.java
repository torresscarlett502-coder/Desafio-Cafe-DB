package sv.udb.cafedonbosco.dto.response;

public class CategoriaResponseDTO {

    private Integer id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private int cantidadProductos;

    public CategoriaResponseDTO() {
    }

    public CategoriaResponseDTO(Integer id, String nombre, String descripcion, Boolean activo, int cantidadProductos) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
        this.cantidadProductos = cantidadProductos;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    public void setCantidadProductos(int cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }
}
