package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.InventarioDAO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.Inventario;
import sv.udb.cafedonbosco.model.MovimientoInventario;
import sv.udb.cafedonbosco.model.TipoMovimiento;
import sv.udb.cafedonbosco.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAOImpl implements InventarioDAO {

    @Override
    public List<Inventario> listarTodos() {
        String sql = "SELECT id, producto_id, cantidad, stock_minimo FROM inventario";
        List<Inventario> inventarios = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                inventarios.add(mapear(rs));
            }
            return inventarios;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar el inventario", e);
        }
    }

    @Override
    public Inventario buscarPorProducto(int productoId) {
        String sql = "SELECT id, producto_id, cantidad, stock_minimo FROM inventario WHERE producto_id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar el inventario del producto", e);
        }
    }

    @Override
    public boolean existeParaProducto(Connection conexion, int productoId) {
        String sql = "SELECT 1 FROM inventario WHERE producto_id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al verificar el inventario del producto", e);
        }
    }

    @Override
    public int obtenerCantidadActual(Connection conexion, int productoId) {
        String sql = "SELECT cantidad FROM inventario WHERE producto_id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al leer la cantidad de inventario", e);
        }
    }

    @Override
    public void crear(Connection conexion, Inventario inventario) {
        String sql = "INSERT INTO inventario (producto_id, cantidad, stock_minimo) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, inventario.getProductoId());
            stmt.setInt(2, inventario.getCantidad());
            stmt.setInt(3, inventario.getStockMinimo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al crear el registro de inventario", e);
        }
    }

    @Override
    public void actualizarStockMinimo(Connection conexion, int productoId, int stockMinimo) {
        String sql = "UPDATE inventario SET stock_minimo = ? WHERE producto_id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, stockMinimo);
            stmt.setInt(2, productoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al actualizar el stock minimo", e);
        }
    }

    @Override
    public void fijarCantidad(Connection conexion, int productoId, int cantidad) {
        String sql = "UPDATE inventario SET cantidad = ? WHERE producto_id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setInt(2, productoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al fijar la cantidad de inventario", e);
        }
    }

    @Override
    public boolean descontarStock(Connection conexion, int productoId, int cantidad) {
        String sql = "UPDATE inventario SET cantidad = cantidad - ? "
                + "WHERE producto_id = ? AND cantidad >= ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setInt(2, productoId);
            stmt.setInt(3, cantidad);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al descontar stock", e);
        }
    }

    @Override
    public void incrementarStock(Connection conexion, int productoId, int cantidad) {
        String sql = "UPDATE inventario SET cantidad = cantidad + ? WHERE producto_id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setInt(2, productoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al incrementar stock", e);
        }
    }

    @Override
    public void registrarMovimiento(Connection conexion, MovimientoInventario movimiento) {
        String sql = "INSERT INTO movimiento_inventario "
                + "(producto_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, motivo, "
                + "venta_id, compra_id, usuario_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, movimiento.getProductoId());
            stmt.setString(2, movimiento.getTipoMovimiento().name());
            stmt.setInt(3, movimiento.getCantidad());
            stmt.setInt(4, movimiento.getStockAnterior());
            stmt.setInt(5, movimiento.getStockNuevo());
            stmt.setString(6, movimiento.getMotivo());
            setIntOrNull(stmt, 7, movimiento.getVentaId());
            setIntOrNull(stmt, 8, movimiento.getCompraId());
            setIntOrNull(stmt, 9, movimiento.getUsuarioId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al registrar el movimiento de inventario", e);
        }
    }

    @Override
    public List<MovimientoInventario> listarMovimientos(Integer productoId, int limite) {
        String sql = "SELECT id, producto_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, "
                + "motivo, venta_id, compra_id, usuario_id, fecha FROM movimiento_inventario "
                + (productoId != null ? "WHERE producto_id = ? " : "")
                + "ORDER BY fecha DESC LIMIT ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            int indice = 1;
            if (productoId != null) {
                stmt.setInt(indice++, productoId);
            }
            stmt.setInt(indice, limite);
            List<MovimientoInventario> movimientos = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapearMovimiento(rs));
                }
            }
            return movimientos;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar los movimientos de inventario", e);
        }
    }

    private void setIntOrNull(PreparedStatement stmt, int indice, Integer valor) throws SQLException {
        if (valor != null) {
            stmt.setInt(indice, valor);
        } else {
            stmt.setNull(indice, Types.INTEGER);
        }
    }

    private MovimientoInventario mapearMovimiento(ResultSet rs) throws SQLException {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setId(rs.getInt("id"));
        movimiento.setProductoId(rs.getInt("producto_id"));
        movimiento.setTipoMovimiento(TipoMovimiento.valueOf(rs.getString("tipo_movimiento")));
        movimiento.setCantidad(rs.getInt("cantidad"));
        movimiento.setStockAnterior(rs.getInt("stock_anterior"));
        movimiento.setStockNuevo(rs.getInt("stock_nuevo"));
        movimiento.setMotivo(rs.getString("motivo"));
        movimiento.setVentaId(getIntOrNull(rs, "venta_id"));
        movimiento.setCompraId(getIntOrNull(rs, "compra_id"));
        movimiento.setUsuarioId(getIntOrNull(rs, "usuario_id"));
        Timestamp fecha = rs.getTimestamp("fecha");
        movimiento.setFecha(fecha != null ? fecha.toLocalDateTime() : null);
        return movimiento;
    }

    private Integer getIntOrNull(ResultSet rs, String columna) throws SQLException {
        int valor = rs.getInt(columna);
        return rs.wasNull() ? null : valor;
    }

    private Inventario mapear(ResultSet rs) throws SQLException {
        return new Inventario(
                rs.getInt("id"),
                rs.getInt("producto_id"),
                rs.getInt("cantidad"),
                rs.getInt("stock_minimo")
        );
    }
}
