package sv.udb.cafedonbosco.dao;

import sv.udb.cafedonbosco.model.Proveedor;

import java.util.List;

public interface ProveedorDAO {

    List<Proveedor> listarActivos();

    List<Proveedor> listarTodos();

    Proveedor buscarPorId(int id);

    /** Comparacion case-insensitive; idAExcluir se usa al editar para no chocar contra si mismo. */
    boolean existeNombre(String nombre, Integer idAExcluir);

    Proveedor crear(Proveedor proveedor);

    void actualizar(Proveedor proveedor);
}
