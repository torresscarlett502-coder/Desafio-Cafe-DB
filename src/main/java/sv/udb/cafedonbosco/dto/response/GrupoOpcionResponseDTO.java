package sv.udb.cafedonbosco.dto.response;

import java.util.ArrayList;
import java.util.List;

public class GrupoOpcionResponseDTO {

    private Integer id;
    private String nombre;
    private Boolean obligatorio;
    private Boolean seleccionMultiple;
    private Boolean activo;
    private List<OpcionResponseDTO> opciones = new ArrayList<>();

    public GrupoOpcionResponseDTO() {
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

    public List<OpcionResponseDTO> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionResponseDTO> opciones) {
        this.opciones = opciones;
    }
}
