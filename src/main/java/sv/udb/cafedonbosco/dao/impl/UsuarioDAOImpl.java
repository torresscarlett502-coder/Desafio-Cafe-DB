package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.UsuarioDAO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.Rol;
import sv.udb.cafedonbosco.model.Usuario;
import sv.udb.cafedonbosco.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public Usuario buscarPorCorreo(String correo) {
        String sql = "SELECT id, nombre, apellido, correo, password, rol, activo "
                + "FROM usuario WHERE correo = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, correo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar el usuario por correo", e);
        }
    }

    @Override
    public Usuario buscarPorId(int id) {
        String sql = "SELECT id, nombre, apellido, correo, password, rol, activo "
                + "FROM usuario WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar el usuario por id", e);
        }
    }

    @Override
    public boolean existeCorreo(String correo) {
        String sql = "SELECT 1 FROM usuario WHERE correo = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, correo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al verificar el correo", e);
        }
    }

    @Override
    public Usuario crear(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, apellido, correo, password, rol, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getCorreo());
            stmt.setString(4, usuario.getPassword());
            stmt.setString(5, usuario.getRol().name());
            stmt.setBoolean(6, Boolean.TRUE.equals(usuario.getActivo()));
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    usuario.setId(claves.getInt(1));
                }
            }
            return usuario;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al crear el usuario", e);
        }
    }

    @Override
    public void actualizarPerfil(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre = ?, apellido = ?, correo = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getCorreo());
            stmt.setInt(4, usuario.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al actualizar el perfil del usuario", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("correo"),
                rs.getString("password"),
                Rol.valueOf(rs.getString("rol")),
                rs.getBoolean("activo")
        );
    }
}
