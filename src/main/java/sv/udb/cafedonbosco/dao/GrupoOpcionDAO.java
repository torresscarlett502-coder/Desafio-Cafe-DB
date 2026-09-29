package sv.udb.cafedonbosco.dao;

import sv.udb.cafedonbosco.model.GrupoOpcion;

import java.util.List;

public interface GrupoOpcionDAO {

    /** Sin las opciones anidadas: el service las carga aparte con OpcionDAO. */
    List<GrupoOpcion> listarTodos();

    GrupoOpcion buscarPorId(int id);

    boolean existeNombre(String nombre, Integer idAExcluir);

    GrupoOpcion crear(GrupoOpcion grupo);

    void actualizar(GrupoOpcion grupo);

    /** Grupos activos asociados a un producto (sin opciones anidadas). */
    List<GrupoOpcion> listarPorProducto(int productoId);

    boolean estaAsociadoAProducto(int productoId, int grupoId);

    void asociarAProducto(int productoId, int grupoId);

    void desasociarDeProducto(int productoId, int grupoId);
}
