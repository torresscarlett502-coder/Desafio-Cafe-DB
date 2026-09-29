package sv.udb.cafedonbosco.dto.request;

import java.util.List;

/**
 * Solicitud de venta presencial (POS) creada por el administrador desde
 * la pantalla de "Nueva venta".
 */
public class VentaPresencialRequestDTO {

    private List<CarritoItemRequestDTO> items;
    private String metodoPago;

    public VentaPresencialRequestDTO() {
    }

    public List<CarritoItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<CarritoItemRequestDTO> items) {
        this.items = items;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}
