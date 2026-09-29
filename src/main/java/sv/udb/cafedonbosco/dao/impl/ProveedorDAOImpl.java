package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.ProveedorDAO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.Proveedor;
import sv.udb.cafedonbosco.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAOImpl implements ProveedorDAO {

    private static final String COLUMNAS = "id, nombre, contacto, telefono, correo, direccion, activo";

    @Override
    public List<Proveedor> listarActivos() {
        return listar("SELECT " + COLUMNAS + " FROM proveedor WHERE activo = TRUE ORDER BY nombre");
    }

    @Override
    public List<Proveedor> listarTodos() {
        return listar("SELECT " + COLUMNAS + " FROM proveedor ORDER BY nombre");
    }

    private List<Proveedor> listar(String sql) {
        List<Proveedor> proveedores = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                proveedores.add(mapear(rs));
            }
            return proveedores;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar los proveedores", e);
        }
    }

    @Override
    public Proveedor buscarPorId(int id) {
        String sql = "SELECT " + COLUMNAS + " FROM proveedor WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar el proveedor", e);
        }
    }

    @Override
    public boolean existeNombre(String nombre, Integer idAExcluir) {
        String sql = "SELECT 1 FROM proveedor WHERE LOWER(nombre) = LOWER(?)"
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
            throw new ErrorInternoException("Error al verificar el nombre del proveedor", e);
        }
    }

    @Override
    public Proveedor crear(Proveedor proveedor) {
        String sql = "INSERT INTO proveedor (nombre, contacto, telefono, correo, direccion, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            enlazarCampos(stmt, proveedor);
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    proveedor.setId(claves.getInt(1));
                }
            }
            return proveedor;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al crear el proveedor", e);
        }
    }

    @Override
    public void actualizar(Proveedor proveedor) {
        String sql = "UPDATE proveedor SET nombre = ?, contacto = ?, telefono = ?, correo = ?, "
                + "direccion = ?, activo = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            enlazarCampos(stmt, proveedor);
            stmt.setInt(7, proveedor.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al actualizar el proveedor", e);
        }
    }

    private void enlazarCampos(PreparedStatement stmt, Proveedor proveedor) throws SQLException {
        stmt.setString(1, proveedor.getNombre());
        stmt.setString(2, proveedor.getContacto());
        stmt.setString(3, proveedor.getTelefono());
        stmt.setString(4, proveedor.getCorreo());
        stmt.setString(5, proveedor.getDireccion());
        stmt.setBoolean(6, Boolean.TRUE.equals(proveedor.getActivo()));
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(rs.getInt("id"));
        proveedor.setNombre(rs.getString("nombre"));
        proveedor.setContacto(rs.getString("contacto"));
        proveedor.setTelefono(rs.getString("telefono"));
        proveedor.setCorreo(rs.getString("correo"));
        proveedor.setDireccion(rs.getString("direccion"));
        proveedor.setActivo(rs.getBoolean("activo"));
        return proveedor;
    }
}
