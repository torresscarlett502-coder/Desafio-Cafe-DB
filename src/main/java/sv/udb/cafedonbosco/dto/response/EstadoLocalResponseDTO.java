package sv.udb.cafedonbosco.dto.response;

import java.time.LocalDateTime;

public class EstadoLocalResponseDTO {

    private boolean abierto;
    private LocalDateTime horaActualServidor;
    private String nombreDia;
    private String horaApertura;
    private String horaCierre;
    private String mensaje;

    public EstadoLocalResponseDTO() {
    }

    public EstadoLocalResponseDTO(boolean abierto, LocalDateTime horaActualServidor, String nombreDia,
                                   String horaApertura, String horaCierre, String mensaje) {
        this.abierto = abierto;
        this.horaActualServidor = horaActualServidor;
        this.nombreDia = nombreDia;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
        this.mensaje = mensaje;
    }

    public boolean isAbierto() {
        return abierto;
    }

    public void setAbierto(boolean abierto) {
        this.abierto = abierto;
    }

    public LocalDateTime getHoraActualServidor() {
        return horaActualServidor;
    }

    public void setHoraActualServidor(LocalDateTime horaActualServidor) {
        this.horaActualServidor = horaActualServidor;
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
