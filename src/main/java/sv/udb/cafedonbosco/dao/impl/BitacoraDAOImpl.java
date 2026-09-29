package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.BitacoraDAO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.Bitacora;
import sv.udb.cafedonbosco.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BitacoraDAOImpl implements BitacoraDAO {

    @Override
    public void registrar(Connection conexion, Bitacora entrada) {
        String sql = "INSERT INTO bitacora (usuario_id, accion, entidad, entidad_id, detalle) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            if (entrada.getUsuarioId() != null) {
                stmt.setInt(1, entrada.getUsuarioId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, entrada.getAccion());
            stmt.setString(3, entrada.getEntidad());
            if (entrada.getEntidadId() != null) {
                stmt.setInt(4, entrada.getEntidadId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setString(5, entrada.getDetalle());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al registrar en la bitacora", e);
        }
    }

    @Override
    public List<Bitacora> listar(String entidad, Integer entidadId, int limite) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, usuario_id, accion, entidad, entidad_id, detalle, fecha FROM bitacora WHERE 1=1 ");
        if (entidad != null) {
            sql.append("AND entidad = ? ");
        }
        if (entidadId != null) {
            sql.append("AND entidad_id = ? ");
        }
        sql.append("ORDER BY fecha DESC LIMIT ?");

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql.toString())) {
            int indice = 1;
            if (entidad != null) {
                stmt.setString(indice++, entidad);
            }
            if (entidadId != null) {
                stmt.setInt(indice++, entidadId);
            }
            stmt.setInt(indice, limite);

            List<Bitacora> resultado = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar la bitacora", e);
        }
    }

    private Bitacora mapear(ResultSet rs) throws SQLException {
        Bitacora bitacora = new Bitacora();
        bitacora.setId(rs.getInt("id"));
        int usuarioId = rs.getInt("usuario_id");
        bitacora.setUsuarioId(rs.wasNull() ? null : usuarioId);
        bitacora.setAccion(rs.getString("accion"));
        bitacora.setEntidad(rs.getString("entidad"));
        int entidadId = rs.getInt("entidad_id");
        bitacora.setEntidadId(rs.wasNull() ? null : entidadId);
        bitacora.setDetalle(rs.getString("detalle"));
        Timestamp fecha = rs.getTimestamp("fecha");
        bitacora.setFecha(fecha != null ? fecha.toLocalDateTime() : null);
        return bitacora;
    }
}
