package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.GrupoOpcionDAO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.GrupoOpcion;
import sv.udb.cafedonbosco.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GrupoOpcionDAOImpl implements GrupoOpcionDAO {

    private static final String COLUMNAS = "id, nombre, obligatorio, seleccion_multiple, activo";

    @Override
    public List<GrupoOpcion> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM grupo_opcion ORDER BY nombre";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<GrupoOpcion> grupos = new ArrayList<>();
            while (rs.next()) {
                grupos.add(mapear(rs));
            }
            return grupos;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar los grupos de opciones", e);
        }
    }

    @Override
    public GrupoOpcion buscarPorId(int id) {
        String sql = "SELECT " + COLUMNAS + " FROM grupo_opcion WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar el grupo de opciones", e);
        }
    }

    @Override
    public boolean existeNombre(String nombre, Integer idAExcluir) {
        String sql = "SELECT 1 FROM grupo_opcion WHERE LOWER(nombre) = LOWER(?)"
                + (idAExcluir != null ? " AND id <> ?" : "");
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            if (idAExcluir != null) {
                stmt.setInt(2, idAExcluir);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al verificar el nombre del grupo de opciones", e);
        }
    }

    @Override
    public GrupoOpcion crear(GrupoOpcion grupo) {
        String sql = "INSERT INTO grupo_opcion (nombre, obligatorio, seleccion_multiple, activo) VALUES (?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            enlazarCampos(stmt, grupo);
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    grupo.setId(claves.getInt(1));
                }
            }
            return grupo;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al crear el grupo de opciones", e);
        }
    }

    @Override
    public void actualizar(GrupoOpcion grupo) {
        String sql = "UPDATE grupo_opcion SET nombre = ?, obligatorio = ?, seleccion_multiple = ?, activo = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            enlazarCampos(stmt, grupo);
            stmt.setInt(5, grupo.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al actualizar el grupo de opciones", e);
        }
    }

    @Override
    public List<GrupoOpcion> listarPorProducto(int productoId) {
        String sql = "SELECT g.id, g.nombre, g.obligatorio, g.seleccion_multiple, g.activo "
                + "FROM grupo_opcion g "
                + "JOIN producto_grupo_opcion pg ON pg.grupo_id = g.id "
                + "WHERE pg.producto_id = ? AND g.activo = TRUE "
                + "ORDER BY g.obligatorio DESC, g.nombre";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            List<GrupoOpcion> grupos = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grupos.add(mapear(rs));
                }
            }
            return grupos;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar los grupos de opciones del producto", e);
        }
    }

    @Override
    public boolean estaAsociadoAProducto(int productoId, int grupoId) {
        String sql = "SELECT 1 FROM producto_grupo_opcion WHERE producto_id = ? AND grupo_id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            stmt.setInt(2, grupoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al verificar la asociacion del grupo con el producto", e);
        }
    }

    @Override
    public void asociarAProducto(int productoId, int grupoId) {
        String sql = "INSERT IGNORE INTO producto_grupo_opcion (producto_id, grupo_id) VALUES (?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            stmt.setInt(2, grupoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al asociar el grupo de opciones al producto", e);
        }
    }

    @Override
    public void desasociarDeProducto(int productoId, int grupoId) {
        String sql = "DELETE FROM producto_grupo_opcion WHERE producto_id = ? AND grupo_id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            stmt.setInt(2, grupoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al desasociar el grupo de opciones del producto", e);
        }
    }

    private void enlazarCampos(PreparedStatement stmt, GrupoOpcion grupo) throws SQLException {
        stmt.setString(1, grupo.getNombre());
        stmt.setBoolean(2, Boolean.TRUE.equals(grupo.getObligatorio()));
        stmt.setBoolean(3, Boolean.TRUE.equals(grupo.getSeleccionMultiple()));
        stmt.setBoolean(4, grupo.getActivo() == null || grupo.getActivo());
    }

    private GrupoOpcion mapear(ResultSet rs) throws SQLException {
        GrupoOpcion grupo = new GrupoOpcion();
        grupo.setId(rs.getInt("id"));
        grupo.setNombre(rs.getString("nombre"));
        grupo.setObligatorio(rs.getBoolean("obligatorio"));
        grupo.setSeleccionMultiple(rs.getBoolean("seleccion_multiple"));
        grupo.setActivo(rs.getBoolean("activo"));
        return grupo;
    }
}
