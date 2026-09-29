package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.CategoriaDAO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.Categoria;
import sv.udb.cafedonbosco.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAOImpl implements CategoriaDAO {

    @Override
    public List<Categoria> listarActivas() {
        return listar("SELECT id, nombre, descripcion, activo FROM categoria WHERE activo = TRUE ORDER BY nombre");
    }

    @Override
    public List<Categoria> listarTodas() {
        return listar("SELECT id, nombre, descripcion, activo FROM categoria ORDER BY nombre");
    }

    private List<Categoria> listar(String sql) {
        List<Categoria> categorias = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categorias.add(mapear(rs));
            }
            return categorias;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar las categorias", e);
        }
    }

    @Override
    public Categoria buscarPorId(int id) {
        String sql = "SELECT id, nombre, descripcion, activo FROM categoria WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar la categoria", e);
        }
    }

    @Override
    public Categoria crear(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre, descripcion, activo) VALUES (?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoria.getNombre());
            stmt.setString(2, categoria.getDescripcion());
            stmt.setBoolean(3, Boolean.TRUE.equals(categoria.getActivo()));
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    categoria.setId(claves.getInt(1));
                }
            }
            return categoria;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al crear la categoria", e);
        }
    }

    @Override
    public void actualizar(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ?, activo = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNombre());
            stmt.setString(2, categoria.getDescripcion());
            stmt.setBoolean(3, Boolean.TRUE.equals(categoria.getActivo()));
            stmt.setInt(4, categoria.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al actualizar la categoria", e);
        }
    }

    @Override
    public int contarProductosActivos(int categoriaId) {
        String sql = "SELECT COUNT(*) FROM producto WHERE categoria_id = ? AND activo = TRUE";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al contar productos de la categoria", e);
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("activo")
        );
    }
}
