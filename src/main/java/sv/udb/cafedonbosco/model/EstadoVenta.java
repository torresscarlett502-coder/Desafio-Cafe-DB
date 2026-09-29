package sv.udb.cafedonbosco.model;

/**
 * Estado del PEDIDO (preparacion/entrega), independiente del estado del
 * pago (ver EstadoPago). Una venta presencial nace ya COMPLETADA en el
 * sentido de que se entrega al momento, pero igual recorre el mismo
 * ciclo para no duplicar logica entre PRESENCIAL y WEB.
 */
public enum EstadoVenta {
    RECIBIDO,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO
}
