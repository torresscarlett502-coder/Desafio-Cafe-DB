package sv.udb.cafedonbosco.util;

import sv.udb.cafedonbosco.exception.TransicionInvalidaException;
import sv.udb.cafedonbosco.model.EstadoVenta;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Unica fuente de verdad sobre que cambios de estado de un pedido son
 * validos. Nadie debe mutar Venta.estado sin pasar antes por aqui.
 */
public final class TransicionEstadoValidator {

    private static final Map<EstadoVenta, Set<EstadoVenta>> TRANSICIONES_PERMITIDAS = new EnumMap<>(EstadoVenta.class);

    static {
        TRANSICIONES_PERMITIDAS.put(EstadoVenta.RECIBIDO, EnumSet.of(EstadoVenta.EN_PREPARACION, EstadoVenta.CANCELADO));
        TRANSICIONES_PERMITIDAS.put(EstadoVenta.EN_PREPARACION, EnumSet.of(EstadoVenta.LISTO, EstadoVenta.CANCELADO));
        TRANSICIONES_PERMITIDAS.put(EstadoVenta.LISTO, EnumSet.of(EstadoVenta.ENTREGADO, EstadoVenta.CANCELADO));
        TRANSICIONES_PERMITIDAS.put(EstadoVenta.ENTREGADO, EnumSet.noneOf(EstadoVenta.class));
        TRANSICIONES_PERMITIDAS.put(EstadoVenta.CANCELADO, EnumSet.noneOf(EstadoVenta.class));
    }

    private TransicionEstadoValidator() {
    }

    public static boolean esValida(EstadoVenta actual, EstadoVenta nuevo) {
        return TRANSICIONES_PERMITIDAS.getOrDefault(actual, Set.of()).contains(nuevo);
    }

    public static boolean esTerminal(EstadoVenta estado) {
        return estado == EstadoVenta.ENTREGADO || estado == EstadoVenta.CANCELADO;
    }

    /** Lanza TransicionInvalidaException con un mensaje claro si el cambio no esta permitido. */
    public static void validar(EstadoVenta actual, EstadoVenta nuevo) {
        if (actual == nuevo) {
            throw new TransicionInvalidaException("La venta ya esta en estado " + actual + ".");
        }
        if (esTerminal(actual)) {
            throw new TransicionInvalidaException(
                    "La venta ya esta en un estado final (" + actual + ") y no admite mas cambios.");
        }
        if (!esValida(actual, nuevo)) {
            throw new TransicionInvalidaException(
                    "No se puede pasar de " + actual + " a " + nuevo + ".");
        }
    }
}
