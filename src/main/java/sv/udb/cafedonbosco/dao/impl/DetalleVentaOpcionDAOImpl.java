package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.DetalleVentaOpcionDAO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.OpcionSeleccionada;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetalleVentaOpcionDAOImpl implements DetalleVentaOpcionDAO {

    @Override
    public void registrar(Connection conexion, int detalleVentaId, OpcionSeleccionada opcion) {
        String sql = "INSERT INTO detalle_venta_opcion "
                + "(detalle_venta_id, opcion_id, nombre_grupo, nombre_opcion, precio_aplicado) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, detalleVentaId);
            if (opcion.getOpcionId() != null) {
                stmt.setInt(2, opcion.getOpcionId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, opcion.getNombreGrupo());
            stmt.setString(4, opcion.getNombreOpcion());
            stmt.setBigDecimal(5, opcion.getPrecioAdicional());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al registrar la opcion elegida del detalle de venta", e);
        }
    }

    @Override
    public List<OpcionSeleccionada> listarPorDetalle(Connection conexion, int detalleVentaId) {
        String sql = "SELECT opcion_id, nombre_grupo, nombre_opcion, precio_aplicado "
                + "FROM detalle_venta_opcion WHERE detalle_venta_id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, detalleVentaId);
            List<OpcionSeleccionada> opciones = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int opcionId = rs.getInt("opcion_id");
                    opciones.add(new OpcionSeleccionada(
                            rs.wasNull() ? null : opcionId,
                            rs.getString("nombre_grupo"),
                            rs.getString("nombre_opcion"),
                            rs.getBigDecimal("precio_aplicado")
                    ));
                }
            }
            return opciones;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar las opciones del detalle de venta", e);
        }
    }

    @Override
    public Map<Integer, List<OpcionSeleccionada>> listarPorDetalles(Connection conexion, List<Integer> detalleVentaIds) {
        Map<Integer, List<OpcionSeleccionada>> resultado = new HashMap<>();
        if (detalleVentaIds.isEmpty()) {
            return resultado;
        }
        String marcadores = String.join(",", Collections.nCopies(detalleVentaIds.size(), "?"));
        String sql = "SELECT detalle_venta_id, opcion_id, nombre_grupo, nombre_opcion, precio_aplicado "
                + "FROM detalle_venta_opcion WHERE detalle_venta_id IN (" + marcadores + ")";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            int indice = 1;
            for (Integer detalleVentaId : detalleVentaIds) {
                stmt.setInt(indice++, detalleVentaId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int detalleVentaId = rs.getInt("detalle_venta_id");
                    int opcionId = rs.getInt("opcion_id");
                    OpcionSeleccionada opcion = new OpcionSeleccionada(
                            rs.wasNull() ? null : opcionId,
                            rs.getString("nombre_grupo"),
                            rs.getString("nombre_opcion"),
                            rs.getBigDecimal("precio_aplicado")
                    );
                    resultado.computeIfAbsent(detalleVentaId, k -> new ArrayList<>()).add(opcion);
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar las opciones de los detalles de venta", e);
        }
    }
}
