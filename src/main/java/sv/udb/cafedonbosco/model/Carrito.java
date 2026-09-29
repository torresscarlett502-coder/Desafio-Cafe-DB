package sv.udb.cafedonbosco.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carrito de compras del consumidor. Se guarda como atributo de
 * HttpSession (ver Constantes.SESSION_CARRITO), por lo que implementa
 * Serializable.
 *
 * Cada linea se identifica por CarritoItem.getClaveLinea() (producto +
 * personalizacion exacta), NO solo por productoId: el mismo producto con
 * dos personalizaciones distintas vive en dos lineas separadas, y solo se
 * agrupan cantidades cuando la personalizacion es exactamente la misma.
 */
public class Carrito implements Serializable {

    private final Map<String, CarritoItem> items = new LinkedHashMap<>();

    public void agregarProducto(CarritoItem nuevo) {
        String claveLinea = nuevo.getClaveLinea();
        CarritoItem existente = items.get(claveLinea);
        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + nuevo.getCantidad());
        } else {
            items.put(claveLinea, nuevo);
        }
    }

    public void actualizarCantidad(String claveLinea, Integer cantidad) {
        CarritoItem item = items.get(claveLinea);
        if (item != null) {
            item.setCantidad(cantidad);
        }
    }

    public void eliminarProducto(String claveLinea) {
        items.remove(claveLinea);
    }

    public void vaciar() {
        items.clear();
    }

    public Map<String, CarritoItem> getItems() {
        return items;
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public BigDecimal calcularSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CarritoItem item : items.values()) {
            subtotal = subtotal.add(item.getSubtotal());
        }
        return subtotal;
    }

    public int contarUnidades() {
        int total = 0;
        for (CarritoItem item : items.values()) {
            total += item.getCantidad();
        }
        return total;
    }
}
