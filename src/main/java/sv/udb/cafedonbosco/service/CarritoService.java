package sv.udb.cafedonbosco.service;

import sv.udb.cafedonbosco.dto.response.CarritoResponseDTO;
import sv.udb.cafedonbosco.model.Carrito;

import java.util.List;

public interface CarritoService {

    void agregarProducto(Carrito carrito, int productoId, int cantidad);

    /** opcionIds puede ser null o vacio si el producto no lleva personalizacion. */
    void agregarProducto(Carrito carrito, int productoId, int cantidad, List<Integer> opcionIds);

    /** claveLinea identifica una linea exacta (producto + personalizacion), ver CarritoItem.getClaveLinea(). */
    void actualizarCantidad(Carrito carrito, String claveLinea, int cantidad);

    void eliminarProducto(Carrito carrito, String claveLinea);

    void vaciar(Carrito carrito);

    CarritoResponseDTO obtenerResumen(Carrito carrito);
}
