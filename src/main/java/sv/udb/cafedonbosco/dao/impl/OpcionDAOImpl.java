package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.OpcionDAO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.Opcion;
import sv.udb.cafedonbosco.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OpcionDAOImpl implements OpcionDAO {

    private static final String COLUMNAS = "id, grupo_id, nombre, precio_adicional, activo";

    @Override
    public List<Opcion> listarPorGrupo(int grupoId) {
        return listar("SELECT " + COLUMNAS + " FROM opcion WHERE grupo_id = ? ORDER BY nombre", grupoId);
    }

    /**
     * Se usa para renderizar la ficha de producto (donde la primera opcion
     * de un grupo de seleccion unica queda pre-marcada por defecto) y para
     * resolver los precios en el checkout. Se ordena por precio adicional
     * ascendente para que ese default sea siempre la opcion mas barata del
     * grupo, nunca una que agregue costo solo porque su nombre viene
     * primero en el alfabeto (ej. "Leche de almendra" antes que "Leche
     * entera"). Entre opciones con el mismo precio se desempata por id
     * (orden de creacion), no por nombre: alfabeticamente "Extra dulce"
     * gana a "Normal", que es justo la opcion que un cliente esperaria
     * por defecto.
     */
    @Override
    public List<Opcion> listarActivasPorGrupo(int grupoId) {
        return listar("SELECT " + COLUMNAS + " FROM opcion WHERE grupo_id = ? AND activo = TRUE ORDER BY precio_adicional, id", grupoId);
    }

    private List<Opcion> listar(String sql, int grupoId) {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, grupoId);
            List<Opcion> opciones = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    opciones.add(mapear(rs));
                }
            }
            return opciones;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar las opciones del grupo", e);
        }
    }

    @Override
    public Opcion buscarPorId(int id) {
        String sql = "SELECT " + COLUMNAS + " FROM opcion WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar la opcion", e);
        }
    }

    @Override
    public boolean existeNombreEnGrupo(int grupoId, String nombre, Integer idAExcluir) {
        String sql = "SELECT 1 FROM opcion WHERE grupo_id = ? AND LOWER(nombre) = LOWER(?)"
                + (idAExcluir != null ? " AND id <> ?" : "");
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, grupoId);
            stmt.setString(2, nombre);
            if (idAExcluir != null) {
                stmt.setInt(3, idAExcluir);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al verificar el nombre de la opcion", e);
        }
    }

    @Override
    public Opcion crear(Opcion opcion) {
        String sql = "INSERT INTO opcion (grupo_id, nombre, precio_adicional, activo) VALUES (?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            enlazarCampos(stmt, opcion);
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    opcion.setId(claves.getInt(1));
                }
            }
            return opcion;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al crear la opcion", e);
        }
    }

    @Override
    public void actualizar(Opcion opcion) {
        String sql = "UPDATE opcion SET grupo_id = ?, nombre = ?, precio_adicional = ?, activo = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            enlazarCampos(stmt, opcion);
            stmt.setInt(5, opcion.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al actualizar la opcion", e);
        }
    }

    private void enlazarCampos(PreparedStatement stmt, Opcion opcion) throws SQLException {
        stmt.setInt(1, opcion.getGrupoId());
        stmt.setString(2, opcion.getNombre());
        stmt.setBigDecimal(3, opcion.getPrecioAdicional());
        stmt.setBoolean(4, opcion.getActivo() == null || opcion.getActivo());
    }

    private Opcion mapear(ResultSet rs) throws SQLException {
        Opcion opcion = new Opcion();
        opcion.setId(rs.getInt("id"));
        opcion.setGrupoId(rs.getInt("grupo_id"));
        opcion.setNombre(rs.getString("nombre"));
        opcion.setPrecioAdicional(rs.getBigDecimal("precio_adicional"));
        opcion.setActivo(rs.getBoolean("activo"));
        return opcion;
    }
}
