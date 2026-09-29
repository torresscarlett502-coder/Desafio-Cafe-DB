package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.CompraDAO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.Compra;
import sv.udb.cafedonbosco.model.DetalleCompra;
import sv.udb.cafedonbosco.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CompraDAOImpl implements CompraDAO {

    @Override
    public Compra crear(Connection conexion, Compra compra) {
        String sql = "INSERT INTO compra (proveedor_id, proveedor_nombre, usuario_id, total) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, compra.getProveedorId());
            stmt.setString(2, compra.getProveedorNombre());
            stmt.setInt(3, compra.getUsuarioId());
            stmt.setBigDecimal(4, compra.getTotal());
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    compra.setId(claves.getInt(1));
                }
            }
            return compra;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al registrar la compra", e);
        }
    }

    @Override
    public void crearDetalle(Connection conexion, DetalleCompra detalle, int compraId) {
        String sql = "INSERT INTO detalle_compra (compra_id, producto_id, cantidad, costo_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, compraId);
            stmt.setInt(2, detalle.getProductoId());
            stmt.setInt(3, detalle.getCantidad());
            stmt.setBigDecimal(4, detalle.getCostoUnitario());
            stmt.setBigDecimal(5, detalle.getSubtotal());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al registrar el detalle de la compra", e);
        }
    }

    @Override
    public List<Compra> listarTodas() {
        String sql = "SELECT id, proveedor_id, proveedor_nombre, usuario_id, total, fecha "
                + "FROM compra ORDER BY fecha DESC";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Compra> compras = new ArrayList<>();
            while (rs.next()) {
                compras.add(mapear(rs));
            }
            return compras;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar las compras", e);
        }
    }

    @Override
    public Compra buscarPorId(int id) {
        String sql = "SELECT id, proveedor_id, proveedor_nombre, usuario_id, total, fecha "
                + "FROM compra WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Compra compra = mapear(rs);
                compra.setDetalles(buscarDetalles(conexion, compra.getId()));
                return compra;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar la compra", e);
        }
    }

    private List<DetalleCompra> buscarDetalles(Connection conexion, int compraId) throws SQLException {
        String sql = "SELECT id, compra_id, producto_id, cantidad, costo_unitario, subtotal "
                + "FROM detalle_compra WHERE compra_id = ?";
        List<DetalleCompra> detalles = new ArrayList<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, compraId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DetalleCompra detalle = new DetalleCompra(
                            rs.getInt("producto_id"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("costo_unitario"),
                            rs.getBigDecimal("subtotal")
                    );
                    detalle.setId(rs.getInt("id"));
                    detalle.setCompraId(rs.getInt("compra_id"));
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    private Compra mapear(ResultSet rs) throws SQLException {
        Compra compra = new Compra();
        compra.setId(rs.getInt("id"));
        compra.setProveedorId(rs.getInt("proveedor_id"));
        compra.setProveedorNombre(rs.getString("proveedor_nombre"));
        compra.setUsuarioId(rs.getInt("usuario_id"));
        compra.setTotal(rs.getBigDecimal("total"));
        Timestamp fecha = rs.getTimestamp("fecha");
        compra.setFecha(fecha != null ? fecha.toLocalDateTime() : null);
        return compra;
    }
}
