package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.HorarioAtencionDAO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.HorarioAtencion;
import sv.udb.cafedonbosco.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HorarioAtencionDAOImpl implements HorarioAtencionDAO {

    private static final String COLUMNAS = "id, dia_semana, nombre_dia, hora_apertura, hora_cierre, "
            + "permitir_pedidos_app, permitir_pedidos_local";

    @Override
    public HorarioAtencion obtenerPorDia(int diaSemana) {
        String sql = "SELECT " + COLUMNAS + " FROM horario_atencion WHERE dia_semana = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, diaSemana);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar el horario de atencion", e);
        }
    }

    @Override
    public List<HorarioAtencion> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM horario_atencion ORDER BY dia_semana";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<HorarioAtencion> horarios = new ArrayList<>();
            while (rs.next()) {
                horarios.add(mapear(rs));
            }
            return horarios;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar los horarios de atencion", e);
        }
    }

    @Override
    public void actualizar(HorarioAtencion horario) {
        String sql = "UPDATE horario_atencion SET hora_apertura = ?, hora_cierre = ?, "
                + "permitir_pedidos_app = ?, permitir_pedidos_local = ? WHERE dia_semana = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setObject(1, horario.getHoraApertura());
            stmt.setObject(2, horario.getHoraCierre());
            stmt.setBoolean(3, Boolean.TRUE.equals(horario.getPermitirPedidosApp()));
            stmt.setBoolean(4, Boolean.TRUE.equals(horario.getPermitirPedidosLocal()));
            stmt.setInt(5, horario.getDiaSemana());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al actualizar el horario de atencion", e);
        }
    }

    private HorarioAtencion mapear(ResultSet rs) throws SQLException {
        HorarioAtencion horario = new HorarioAtencion();
        horario.setId(rs.getInt("id"));
        horario.setDiaSemana(rs.getInt("dia_semana"));
        horario.setNombreDia(rs.getString("nombre_dia"));
        horario.setHoraApertura(toLocalTime(rs.getTime("hora_apertura")));
        horario.setHoraCierre(toLocalTime(rs.getTime("hora_cierre")));
        horario.setPermitirPedidosApp(rs.getBoolean("permitir_pedidos_app"));
        horario.setPermitirPedidosLocal(rs.getBoolean("permitir_pedidos_local"));
        return horario;
    }

    private LocalTime toLocalTime(java.sql.Time hora) {
        return hora != null ? hora.toLocalTime() : null;
    }
}
