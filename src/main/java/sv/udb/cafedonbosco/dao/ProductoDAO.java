package sv.udb.cafedonbosco.dao;

import sv.udb.cafedonbosco.model.Producto;

import java.sql.Connection;
import java.util.List;

public interface ProductoDAO {

    /** Activos y de categoria activa; usado donde no hacen falta filtros. */
    List<Producto> listarActivos();

    List<Producto> listarActivosPorCategoria(int categoriaId);

    /**
     * Catalogo publico combinando categoria + busqueda + orden en una
     * sola consulta (categoriaId y busqueda pueden ser null = sin ese
     * filtro). "popularidad" ordena por unidades vendidas reales.
     */
    List<Producto> buscarCatalogo(Integer categoriaId, String busqueda, String orden);

    List<Producto> listarTodos();

    Producto buscarPorId(int id);

    /**
     * Crea el producto dentro de una transaccion ya abierta, para poder
     * insertar en la misma operacion su registro de inventario inicial.
     */
    Producto crear(Connection conexion, Producto producto);

    /**
     * Actualiza el producto dentro de una transaccion ya abierta, para
     * poder sincronizar en la misma operacion el stock minimo de su
     * inventario (ver ProductoServiceImpl.actualizar).
     */
    void actualizar(Connection conexion, Producto producto);

    void cambiarEstado(int id, boolean activo);

    boolean existeNombreEnCategoria(int categoriaId, String nombre, Integer idAExcluir);
}
