package sv.udb.cafedonbosco.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Grupo de personalizacion que un producto puede ofrecer (ej. "Tipo de
 * leche", "Nivel de azucar"). seleccionMultiple=false exige elegir a lo
 * sumo una opcion del grupo; obligatorio=true exige elegir al menos una.
 */
public class GrupoOpcion {

    private Integer id;
    private String nombre;
    private Boolean obligatorio;
    private Boolean seleccionMultiple;
    private Boolean activo;
    private List<Opcion> opciones = new ArrayList<>();

    public GrupoOpcion() {
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

    public List<Opcion> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<Opcion> opciones) {
        this.opciones = opciones;
    }
}
