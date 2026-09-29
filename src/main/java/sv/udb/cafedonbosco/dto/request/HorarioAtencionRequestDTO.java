package sv.udb.cafedonbosco.dto.request;

public class HorarioAtencionRequestDTO {

    /** Formato "HH:mm" o "HH:mm:ss". */
    private String horaApertura;
    private String horaCierre;
    private Boolean permitirPedidosApp;
    private Boolean permitirPedidosLocal;

    public HorarioAtencionRequestDTO() {
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
