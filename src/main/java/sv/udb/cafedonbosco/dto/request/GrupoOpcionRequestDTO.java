package sv.udb.cafedonbosco.dto.request;

public class GrupoOpcionRequestDTO {

    private String nombre;
    private Boolean obligatorio;
    private Boolean seleccionMultiple;
    private Boolean activo;

    public GrupoOpcionRequestDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getObligatorio() {
        return obligatorio;
    }

    public void setObligatorio(Boolean obligatorio) {
        this.obligatorio = obligatorio;
    }

    public Boolean getSeleccionMultiple() {
        return seleccionMultiple;
    }

    public void setSeleccionMultiple(Boolean seleccionMultiple) {
        this.seleccionMultiple = seleccionMultiple;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
