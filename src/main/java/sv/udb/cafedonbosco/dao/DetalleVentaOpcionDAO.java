package sv.udb.cafedonbosco.dao;

import sv.udb.cafedonbosco.model.OpcionSeleccionada;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface DetalleVentaOpcionDAO {

    /** Se registra dentro de la misma transaccion que el detalle_venta al que pertenece. */
    void registrar(Connection conexion, int detalleVentaId, OpcionSeleccionada opcion);

    /** Se lee con la misma conexion que ya esta abierta para leer el detalle_venta (ver VentaDAOImpl.buscarDetalles). */
    List<OpcionSeleccionada> listarPorDetalle(Connection conexion, int detalleVentaId);

    /**
     * Trae las opciones de varios detalles de venta en una sola consulta
     * IN (...), indexadas por detalle_venta_id (ver
     * VentaDAOImpl.cargarDetallesEnLote, que lista muchas ventas a la vez
     * y necesita evitar una consulta por cada detalle).
     */
    Map<Integer, List<OpcionSeleccionada>> listarPorDetalles(Connection conexion, List<Integer> detalleVentaIds);
}
