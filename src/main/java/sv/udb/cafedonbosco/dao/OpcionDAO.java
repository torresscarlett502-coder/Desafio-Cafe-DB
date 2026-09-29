package sv.udb.cafedonbosco.dao;

import sv.udb.cafedonbosco.model.Opcion;

import java.util.List;

public interface OpcionDAO {

    List<Opcion> listarPorGrupo(int grupoId);

    List<Opcion> listarActivasPorGrupo(int grupoId);

    Opcion buscarPorId(int id);

    boolean existeNombreEnGrupo(int grupoId, String nombre, Integer idAExcluir);

    Opcion crear(Opcion opcion);

    void actualizar(Opcion opcion);
}
