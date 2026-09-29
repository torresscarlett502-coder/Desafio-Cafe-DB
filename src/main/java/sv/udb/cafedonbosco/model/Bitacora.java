package sv.udb.cafedonbosco.model;

import java.time.LocalDateTime;

/**
 * Bitacora de auditoria para operaciones administrativas relevantes
 * (crear/editar producto, ajustar inventario, registrar compra, cambiar
 * estado o pago de una venta, cancelar). No pretende auditar cada
 * lectura, solo las escrituras que importan para rendir cuentas.
 */
public class Bitacora {

    private Integer id;
    private Integer usuarioId;
    private String accion;
    private String entidad;
    private Integer entidadId;
    private String detalle;
    private LocalDateTime fecha;

    public Bitacora() {
    }

    public Bitacora(Integer usuarioId, String accion, String entidad, Integer entidadId, String detalle) {
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.detalle = detalle;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public Integer getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Integer entidadId) {
        this.entidadId = entidadId;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
