package sv.udb.cafedonbosco.dto.request;

import java.util.List;

public class CarritoItemRequestDTO {

    private Integer productoId;
    private Integer cantidad;
    /** Opcional: ids de Opcion elegidas (tipo de leche, azucar, etc.). */
    private List<Integer> opcionIds;

    public CarritoItemRequestDTO() {
    }

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public List<Integer> getOpcionIds() {
        return opcionIds;
    }

    public void setOpcionIds(List<Integer> opcionIds) {
        this.opcionIds = opcionIds;
    }
}
