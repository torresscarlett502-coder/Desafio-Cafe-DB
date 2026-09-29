package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.DetalleVentaOpcionDAO;
import sv.udb.cafedonbosco.dao.VentaDAO;
import sv.udb.cafedonbosco.dto.request.VentaFiltroDTO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.DetalleVenta;
import sv.udb.cafedonbosco.model.EstadoPago;
import sv.udb.cafedonbosco.model.EstadoVenta;
import sv.udb.cafedonbosco.model.OpcionSeleccionada;
import sv.udb.cafedonbosco.model.TipoVenta;
import sv.udb.cafedonbosco.model.Venta;
import sv.udb.cafedonbosco.util.ConexionBD;
import sv.udb.cafedonbosco.util.Constantes;
import sv.udb.cafedonbosco.util.FechaUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VentaDAOImpl implements VentaDAO {

    private final DetalleVentaOpcionDAO detalleVentaOpcionDAO = new DetalleVentaOpcionDAOImpl();

    private static final String COLUMNAS_VENTA =
            "id, usuario_id, tipo_venta, estado, subtotal, envio, total, metodo_pago, "
                    + "estado_pago, tipo_entrega, nombre_cliente, correo_cliente, telefono_cliente, "
                    + "direccion_cliente, notas, token_ticket, idempotency_key, fecha, "
                    + "fecha_inicio_preparacion, fecha_estimada_listo, fecha_listo, fecha_entregado, "
                    + "actualizado_en";

    @Override
    public Venta crear(Connection conexion, Venta venta) {
        String sql = "INSERT INTO venta (usuario_id, tipo_venta, estado, subtotal, envio, total, "
                + "metodo_pago, estado_pago, tipo_entrega, nombre_cliente, correo_cliente, "
                + "telefono_cliente, direccion_cliente, notas, token_ticket, idempotency_key, fecha) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setIntOrNull(stmt, 1, venta.getUsuarioId());
            stmt.setString(2, venta.getTipoVenta().name());
            stmt.setString(3, venta.getEstado().name());
            stmt.setBigDecimal(4, venta.getSubtotal());
            stmt.setBigDecimal(5, venta.getEnvio());
            stmt.setBigDecimal(6, venta.getTotal());
            stmt.setString(7, venta.getMetodoPago());
            stmt.setString(8, venta.getEstadoPago().name());
            stmt.setString(9, venta.getTipoEntrega());
            stmt.setString(10, venta.getNombreCliente());
            stmt.setString(11, venta.getCorreoCliente());
            stmt.setString(12, venta.getTelefonoCliente());
            stmt.setString(13, venta.getDireccionCliente());
            stmt.setString(14, venta.getNotas());
            stmt.setString(15, venta.getTokenTicket());
            stmt.setString(16, venta.getIdempotencyKey());
            // fecha se fija explicitamente desde FechaUtil (hora real de El
            // Salvador) en vez de depender del DEFAULT CURRENT_TIMESTAMP de
            // la columna, que usaria la zona horaria del servidor de MySQL.
            stmt.setTimestamp(17, Timestamp.valueOf(venta.getFecha()));
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    venta.setId(claves.getInt(1));
                }
            }
            return venta;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al registrar la venta", e);
        }
    }

    @Override
    public void crearDetalle(Connection conexion, DetalleVenta detalle, int ventaId) {
        String sql = "INSERT INTO detalle_venta "
                + "(venta_id, producto_id, nombre_producto, cantidad, precio_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ventaId);
            stmt.setInt(2, detalle.getProductoId());
            stmt.setString(3, detalle.getNombreProducto());
            stmt.setInt(4, detalle.getCantidad());
            stmt.setBigDecimal(5, detalle.getPrecioUnitario());
            stmt.setBigDecimal(6, detalle.getSubtotal());
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    detalle.setId(claves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al registrar el detalle de la venta", e);
        }
    }

    @Override
    public Venta buscarPorId(int id) {
        String sql = "SELECT " + COLUMNAS_VENTA + " FROM venta WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return ejecutarBusquedaUnica(conexion, stmt);
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar la venta", e);
        }
    }

    @Override
    public Venta buscarPorToken(String token) {
        String sql = "SELECT " + COLUMNAS_VENTA + " FROM venta WHERE token_ticket = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, token);
            return ejecutarBusquedaUnica(conexion, stmt);
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar la venta", e);
        }
    }

    @Override
    public Venta buscarPorIdParaActualizar(Connection conexion, int id) {
        String sql = "SELECT " + COLUMNAS_VENTA + " FROM venta WHERE id = ? FOR UPDATE";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Venta venta = mapear(rs);
                venta.setDetalles(buscarDetalles(conexion, venta.getId()));
                return venta;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar la venta para actualizarla", e);
        }
    }

    @Override
    public boolean existeIdempotencyKey(String idempotencyKey) {
        String sql = "SELECT 1 FROM venta WHERE idempotency_key = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, idempotencyKey);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al verificar la clave de idempotencia", e);
        }
    }

    @Override
    public Venta buscarPorIdempotencyKey(String idempotencyKey) {
        String sql = "SELECT " + COLUMNAS_VENTA + " FROM venta WHERE idempotency_key = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, idempotencyKey);
            return ejecutarBusquedaUnica(conexion, stmt);
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar la venta por clave de idempotencia", e);
        }
    }

    private Venta ejecutarBusquedaUnica(Connection conexion, PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            Venta venta = mapear(rs);
            venta.setDetalles(buscarDetalles(conexion, venta.getId()));
            return venta;
        }
    }

    private List<DetalleVenta> buscarDetalles(Connection conexion, int ventaId) throws SQLException {
        String sql = "SELECT id, venta_id, producto_id, nombre_producto, cantidad, precio_unitario, subtotal "
                + "FROM detalle_venta WHERE venta_id = ?";
        List<DetalleVenta> detalles = new ArrayList<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, ventaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta detalle = new DetalleVenta(
                            rs.getInt("producto_id"),
                            rs.getString("nombre_producto"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"),
                            rs.getBigDecimal("subtotal")
                    );
                    detalle.setId(rs.getInt("id"));
                    detalle.setVentaId(rs.getInt("venta_id"));
                    detalle.setOpciones(detalleVentaOpcionDAO.listarPorDetalle(conexion, detalle.getId()));
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    @Override
    public List<Venta> listarHistorial(TipoVenta tipoVenta, int limite) {
        String sql = "SELECT " + COLUMNAS_VENTA + " FROM venta "
                + (tipoVenta != null ? "WHERE tipo_venta = ? " : "")
                + "ORDER BY fecha DESC LIMIT ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            int indice = 1;
            if (tipoVenta != null) {
                stmt.setString(indice++, tipoVenta.name());
            }
            stmt.setInt(indice, limite);
            List<Venta> ventas = ejecutarListado(stmt);
            cargarDetallesEnLote(conexion, ventas);
            return ventas;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar el historial de ventas", e);
        }
    }

    /**
     * El historial es una lista acotada (ver "limite") que el administrador
     * consulta bajo demanda, no un endpoint de alto trafico, asi que una
     * sola consulta IN (...) para traer todos los items es preferible a
     * abrir una consulta por cada venta (N+1).
     */
    private void cargarDetallesEnLote(Connection conexion, List<Venta> ventas) throws SQLException {
        if (ventas.isEmpty()) {
            return;
        }
        Map<Integer, Venta> ventasPorId = new HashMap<>();
        for (Venta venta : ventas) {
            venta.setDetalles(new ArrayList<>());
            ventasPorId.put(venta.getId(), venta);
        }
        String marcadores = String.join(",", Collections.nCopies(ventas.size(), "?"));
        String sql = "SELECT id, venta_id, producto_id, nombre_producto, cantidad, precio_unitario, subtotal "
                + "FROM detalle_venta WHERE venta_id IN (" + marcadores + ")";
        List<DetalleVenta> todosLosDetalles = new ArrayList<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            int indice = 1;
            for (Venta venta : ventas) {
                stmt.setInt(indice++, venta.getId());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Venta venta = ventasPorId.get(rs.getInt("venta_id"));
                    if (venta != null) {
                        DetalleVenta detalle = new DetalleVenta(
                                rs.getInt("producto_id"),
                                rs.getString("nombre_producto"),
                                rs.getInt("cantidad"),
                                rs.getBigDecimal("precio_unitario"),
                                rs.getBigDecimal("subtotal")
                        );
                        detalle.setId(rs.getInt("id"));
                        detalle.setVentaId(rs.getInt("venta_id"));
                        venta.getDetalles().add(detalle);
                        todosLosDetalles.add(detalle);
                    }
                }
            }
        }
        // Igual que el detalle_venta de arriba: una sola consulta IN (...)
        // para las opciones de todos los detalles, en vez de una consulta
        // por detalle (ver DetalleVentaOpcionDAO.listarPorDetalles).
        List<Integer> detalleIds = new ArrayList<>();
        for (DetalleVenta detalle : todosLosDetalles) {
            detalleIds.add(detalle.getId());
        }
        Map<Integer, List<OpcionSeleccionada>> opcionesPorDetalle = detalleVentaOpcionDAO.listarPorDetalles(conexion, detalleIds);
        for (DetalleVenta detalle : todosLosDetalles) {
            detalle.setOpciones(opcionesPorDetalle.get(detalle.getId()));
        }
    }

    @Override
    public List<Venta> listarRecientes(int limite) {
        return listarHistorial(null, limite);
    }

    @Override
    public List<Venta> listarFiltrado(VentaFiltroDTO filtro) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNAS_VENTA + " FROM venta WHERE 1=1 ");
        List<Object> parametros = new ArrayList<>();

        if (filtro.getTipoVenta() != null) {
            sql.append("AND tipo_venta = ? ");
            parametros.add(filtro.getTipoVenta().name());
        }
        if (filtro.getEstado() != null) {
            sql.append("AND estado = ? ");
            parametros.add(filtro.getEstado().name());
        }
        if (filtro.getEstadoPago() != null) {
            sql.append("AND estado_pago = ? ");
            parametros.add(filtro.getEstadoPago().name());
        }
        if (filtro.getFechaDesde() != null) {
            sql.append("AND fecha >= ? ");
            parametros.add(Timestamp.valueOf(filtro.getFechaDesde().atStartOfDay()));
        }
        if (filtro.getFechaHasta() != null) {
            sql.append("AND fecha < ? ");
            parametros.add(Timestamp.valueOf(filtro.getFechaHasta().plusDays(1).atStartOfDay()));
        }
        if (filtro.getCliente() != null && !filtro.getCliente().isBlank()) {
            sql.append("AND (nombre_cliente LIKE ? OR correo_cliente LIKE ?) ");
            String comodin = "%" + filtro.getCliente().trim() + "%";
            parametros.add(comodin);
            parametros.add(comodin);
        }
        if (filtro.getNumeroVenta() != null) {
            sql.append("AND id = ? ");
            parametros.add(filtro.getNumeroVenta());
        }
        if (filtro.getMetodoPago() != null && !filtro.getMetodoPago().isBlank()) {
            sql.append("AND metodo_pago = ? ");
            parametros.add(filtro.getMetodoPago());
        }

        int tamano = Math.min(Math.max(filtro.getSize(), 1), Constantes.TAMANO_PAGINA_MAXIMO);
        int desplazamiento = filtro.getPage() * tamano;
        sql.append("ORDER BY fecha DESC LIMIT ? OFFSET ?");
        parametros.add(tamano);
        parametros.add(desplazamiento);

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            return ejecutarListado(stmt);
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al filtrar el historial de ventas", e);
        }
    }

    @Override
    public List<Venta> listarPorUsuario(int usuarioId, int limite) {
        String sql = "SELECT " + COLUMNAS_VENTA + " FROM venta WHERE usuario_id = ? ORDER BY fecha DESC LIMIT ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, Math.min(Math.max(limite, 1), Constantes.TAMANO_PAGINA_MAXIMO));
            return ejecutarListado(stmt);
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar los pedidos del usuario", e);
        }
    }

    @Override
    public List<Venta> listarEnPreparacionVencidas() {
        String sql = "SELECT " + COLUMNAS_VENTA + " FROM venta "
                + "WHERE estado = 'EN_PREPARACION' AND fecha_estimada_listo IS NOT NULL "
                + "AND fecha_estimada_listo <= NOW()";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            return ejecutarListado(stmt);
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar ventas en preparacion vencidas", e);
        }
    }

    private List<Venta> ejecutarListado(PreparedStatement stmt) throws SQLException {
        List<Venta> ventas = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ventas.add(mapear(rs));
            }
        }
        return ventas;
    }

    @Override
    public void iniciarPreparacion(Connection conexion, int ventaId, LocalDateTime inicio, LocalDateTime estimadaListo) {
        String sql = "UPDATE venta SET estado = 'EN_PREPARACION', fecha_inicio_preparacion = ?, "
                + "fecha_estimada_listo = ? WHERE id = ?";
        ejecutarActualizacion(conexion, sql, stmt -> {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(estimadaListo));
            stmt.setInt(3, ventaId);
        });
    }

    @Override
    public void marcarListo(Connection conexion, int ventaId, LocalDateTime listoEn) {
        String sql = "UPDATE venta SET estado = 'LISTO', fecha_listo = ? WHERE id = ?";
        ejecutarActualizacion(conexion, sql, stmt -> {
            stmt.setTimestamp(1, Timestamp.valueOf(listoEn));
            stmt.setInt(2, ventaId);
        });
    }

    @Override
    public void marcarEntregado(Connection conexion, int ventaId, LocalDateTime entregadoEn) {
        String sql = "UPDATE venta SET estado = 'ENTREGADO', fecha_entregado = ? WHERE id = ?";
        ejecutarActualizacion(conexion, sql, stmt -> {
            stmt.setTimestamp(1, Timestamp.valueOf(entregadoEn));
            stmt.setInt(2, ventaId);
        });
    }

    @Override
    public void marcarCancelado(Connection conexion, int ventaId) {
        String sql = "UPDATE venta SET estado = 'CANCELADO' WHERE id = ?";
        ejecutarActualizacion(conexion, sql, stmt -> stmt.setInt(1, ventaId));
    }

    @Override
    public void actualizarEstadoPago(Connection conexion, int ventaId, EstadoPago nuevoEstadoPago) {
        String sql = "UPDATE venta SET estado_pago = ? WHERE id = ?";
        ejecutarActualizacion(conexion, sql, stmt -> {
            stmt.setString(1, nuevoEstadoPago.name());
            stmt.setInt(2, ventaId);
        });
    }

    private void ejecutarActualizacion(Connection conexion, String sql, EnlazadorParametros enlazador) {
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            enlazador.enlazar(stmt);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al actualizar la venta", e);
        }
    }

    @FunctionalInterface
    private interface EnlazadorParametros {
        void enlazar(PreparedStatement stmt) throws SQLException;
    }

    @Override
    public BigDecimal sumarTotalDelDia() {
        // Una venta con el pago aun PENDIENTE (p. ej. contra entrega o
        // transferencia sin confirmar) todavia no es un ingreso real, asi
        // que no debe sumar al total del dia hasta que se apruebe.
        // "Hoy" se calcula con la hora real de El Salvador (FechaUtil), no con
        // CURDATE(): el servidor de BD corre en UTC, asi que CURDATE() cambia
        // de dia varias horas antes que en El Salvador.
        String sql = "SELECT COALESCE(SUM(total), 0) FROM venta "
                + "WHERE DATE(fecha) = ? AND estado <> 'CANCELADO' AND estado_pago = 'APROBADO'";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setObject(1, FechaUtil.obtenerFechaHoraActual().toLocalDate());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al calcular el total de ventas", e);
        }
    }

    @Override
    public int contarVentasDelDia() {
        String sql = "SELECT COUNT(*) FROM venta WHERE DATE(fecha) = ? AND estado <> 'CANCELADO'";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setObject(1, FechaUtil.obtenerFechaHoraActual().toLocalDate());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al contar las ventas del dia", e);
        }
    }

    @Override
    public BigDecimal sumarTotalDelMes() {
        LocalDateTime ahora = FechaUtil.obtenerFechaHoraActual();
        String sql = "SELECT COALESCE(SUM(total), 0) FROM venta "
                + "WHERE YEAR(fecha) = ? AND MONTH(fecha) = ? "
                + "AND estado <> 'CANCELADO' AND estado_pago = 'APROBADO'";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, ahora.getYear());
            stmt.setInt(2, ahora.getMonthValue());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al calcular el total de ventas", e);
        }
    }

    @Override
    public BigDecimal sumarIngresosCobrados() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM venta "
                + "WHERE estado_pago = 'APROBADO' AND estado <> 'CANCELADO'";
        return ejecutarSuma(sql);
    }

    @Override
    public int contarPorEstado(EstadoVenta estado) {
        String sql = "SELECT COUNT(*) FROM venta WHERE estado = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, estado.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al contar ventas por estado", e);
        }
    }

    private BigDecimal ejecutarSuma(String sql) {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al calcular el total de ventas", e);
        }
    }

    private void setIntOrNull(PreparedStatement stmt, int indice, Integer valor) throws SQLException {
        if (valor != null) {
            stmt.setInt(indice, valor);
        } else {
            stmt.setNull(indice, Types.INTEGER);
        }
    }

    private Venta mapear(ResultSet rs) throws SQLException {
        Venta venta = new Venta();
        venta.setId(rs.getInt("id"));
        int usuarioId = rs.getInt("usuario_id");
        venta.setUsuarioId(rs.wasNull() ? null : usuarioId);
        venta.setTipoVenta(TipoVenta.valueOf(rs.getString("tipo_venta")));
        venta.setEstado(EstadoVenta.valueOf(rs.getString("estado")));
        venta.setSubtotal(rs.getBigDecimal("subtotal"));
        venta.setEnvio(rs.getBigDecimal("envio"));
        venta.setTotal(rs.getBigDecimal("total"));
        venta.setMetodoPago(rs.getString("metodo_pago"));
        venta.setEstadoPago(EstadoPago.valueOf(rs.getString("estado_pago")));
        venta.setTipoEntrega(rs.getString("tipo_entrega"));
        venta.setNombreCliente(rs.getString("nombre_cliente"));
        venta.setCorreoCliente(rs.getString("correo_cliente"));
        venta.setTelefonoCliente(rs.getString("telefono_cliente"));
        venta.setDireccionCliente(rs.getString("direccion_cliente"));
        venta.setNotas(rs.getString("notas"));
        venta.setTokenTicket(rs.getString("token_ticket"));
        venta.setIdempotencyKey(rs.getString("idempotency_key"));
        venta.setFecha(aLocalDateTime(rs.getTimestamp("fecha")));
        venta.setFechaInicioPreparacion(aLocalDateTime(rs.getTimestamp("fecha_inicio_preparacion")));
        venta.setFechaEstimadaListo(aLocalDateTime(rs.getTimestamp("fecha_estimada_listo")));
        venta.setFechaListo(aLocalDateTime(rs.getTimestamp("fecha_listo")));
        venta.setFechaEntregado(aLocalDateTime(rs.getTimestamp("fecha_entregado")));
        venta.setActualizadoEn(aLocalDateTime(rs.getTimestamp("actualizado_en")));
        return venta;
    }

    private LocalDateTime aLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
