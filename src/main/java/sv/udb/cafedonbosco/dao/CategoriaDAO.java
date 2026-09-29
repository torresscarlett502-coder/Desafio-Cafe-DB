package sv.udb.cafedonbosco.dao;

import sv.udb.cafedonbosco.model.Categoria;

import java.util.List;

public interface CategoriaDAO {

    List<Categoria> listarActivas();

    List<Categoria> listarTodas();

    Categoria buscarPorId(int id);

    Categoria crear(Categoria categoria);

    void actualizar(Categoria categoria);

    int contarProductosActivos(int categoriaId);
}
