package sv.udb.cafedonbosco.dao;

import sv.udb.cafedonbosco.model.Inventario;
import sv.udb.cafedonbosco.model.MovimientoInventario;

import java.sql.Connection;
import java.util.List;

public interface InventarioDAO {

    List<Inventario> listarTodos();

    Inventario buscarPorProducto(int productoId);

    boolean existeParaProducto(Connection conexion, int productoId);

    /** Cantidad actual leida dentro de la transaccion en curso. */
    int obtenerCantidadActual(Connection conexion, int productoId);

    void crear(Connection conexion, Inventario inventario);

    void actualizarStockMinimo(Connection conexion, int productoId, int stockMinimo);

    void fijarCantidad(Connection conexion, int productoId, int cantidad);

    /**
     * Descuenta stock de forma atomica dentro de una transaccion
     * existente. La condicion cantidad >= ? evita vender mas unidades de
     * las disponibles aunque dos operaciones intenten descontar al mismo
     * tiempo. Devuelve true solo si la fila se actualizo.
     */
    boolean descontarStock(Connection conexion, int productoId, int cantidad);

    void incrementarStock(Connection conexion, int productoId, int cantidad);

    void registrarMovimiento(Connection conexion, MovimientoInventario movimiento);

    List<MovimientoInventario> listarMovimientos(Integer productoId, int limite);
}
