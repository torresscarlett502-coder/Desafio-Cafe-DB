package sv.udb.cafedonbosco.dto.response;

public class HorarioAtencionResponseDTO {

    private Integer diaSemana;
    private String nombreDia;
    private String horaApertura;
    private String horaCierre;
    private Boolean permitirPedidosApp;
    private Boolean permitirPedidosLocal;

    public HorarioAtencionResponseDTO() {
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

    public String getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(String horaApertura) {
        this.horaApertura = horaApertura;
    }

    public String getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(String horaCierre) {
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
