package sv.udb.cafedonbosco.model;

import java.time.LocalTime;



public class HorarioAtencion {

    private Integer id;
    private Integer diaSemana;
    private String nombreDia;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private Boolean permitirPedidosApp;
    private Boolean permitirPedidosLocal;

    public HorarioAtencion() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(Integer diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getNombreDia() {
        return nombreDia;
    }

    public void setNombreDia(String nombreDia) {
        this.nombreDia = nombreDia;
    }

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalTime horaCierre) {
        this.horaCierre = horaCierre;
    }

    public Boolean getPermitirPedidosApp() {
        return permitirPedidosApp;
    }

    public void setPermitirPedidosApp(Boolean permitirPedidosApp) {
        this.permitirPedidosApp = permitirPedidosApp;
    }

    public Boolean getPermitirPedidosLocal() {
        return permitirPedidosLocal;
    }

    public void setPermitirPedidosLocal(Boolean permitirPedidosLocal) {
        this.permitirPedidosLocal = permitirPedidosLocal;
    }
}
