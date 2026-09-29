package sv.udb.cafedonbosco.dao.impl;

import sv.udb.cafedonbosco.dao.ProductoDAO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.model.Producto;
import sv.udb.cafedonbosco.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAOImpl implements ProductoDAO {

    private static final String COLUMNAS =
            "id, categoria_id, nombre, descripcion, precio, imagen, tiempo_preparacion_minutos, activo";

    private static final String COLUMNAS_P =
            "p.id, p.categoria_id, p.nombre, p.descripcion, p.precio, p.imagen, "
                    + "p.tiempo_preparacion_minutos, p.activo";

    @Override
    public List<Producto> listarActivos() {
        String sql = "SELECT " + COLUMNAS_P + " FROM producto p "
                + "JOIN categoria c ON c.id = p.categoria_id "
                + "WHERE p.activo = TRUE AND c.activo = TRUE ORDER BY p.nombre";
        return listar(sql);
    }

    @Override
    public List<Producto> listarActivosPorCategoria(int categoriaId) {
        String sql = "SELECT " + COLUMNAS_P + " FROM producto p "
                + "JOIN categoria c ON c.id = p.categoria_id "
                + "WHERE p.activo = TRUE AND c.activo = TRUE AND p.categoria_id = ? ORDER BY p.nombre";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            return ejecutarListado(stmt);
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar productos por categoria", e);
        }
    }

    @Override
    public List<Producto> buscarCatalogo(Integer categoriaId, String busqueda, String orden) {
        boolean porPopularidad = "popularidad".equals(orden);

        StringBuilder sql = new StringBuilder("SELECT ").append(COLUMNAS_P);
        if (porPopularidad) {
            sql.append(", COALESCE(SUM(dv.cantidad), 0) AS vendidos ");
        }
        sql.append(" FROM producto p JOIN categoria c ON c.id = p.categoria_id ");
        if (porPopularidad) {
            sql.append("LEFT JOIN detalle_venta dv ON dv.producto_id = p.id ");
        }
        sql.append("WHERE p.activo = TRUE AND c.activo = TRUE ");
        if (categoriaId != null) {
            sql.append("AND p.categoria_id = ? ");
        }
        if (busqueda != null && !busqueda.isBlank()) {
            sql.append("AND (p.nombre LIKE ? OR p.descripcion LIKE ?) ");
        }
        if (porPopularidad) {
            sql.append("GROUP BY ").append(COLUMNAS_P).append(" ORDER BY vendidos DESC, p.nombre ASC");
        } else if ("precio_menor".equals(orden)) {
            sql.append("ORDER BY p.precio ASC");
        } else if ("precio_mayor".equals(orden)) {
            sql.append("ORDER BY p.precio DESC");
        } else {
            sql.append("ORDER BY p.nombre ASC");
        }

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql.toString())) {
            int indice = 1;
            if (categoriaId != null) {
                stmt.setInt(indice++, categoriaId);
            }
            if (busqueda != null && !busqueda.isBlank()) {
                String comodin = "%" + busqueda.trim() + "%";
                stmt.setString(indice++, comodin);
                stmt.setString(indice, comodin);
            }
            return ejecutarListado(stmt);
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar el catalogo de productos", e);
        }
    }

    @Override
    public List<Producto> listarTodos() {
        return listar("SELECT " + COLUMNAS + " FROM producto ORDER BY nombre");
    }

    private List<Producto> listar(String sql) {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            return ejecutarListado(stmt);
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al listar productos", e);
        }
    }

    private List<Producto> ejecutarListado(PreparedStatement stmt) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                productos.add(mapear(rs));
            }
        }
        return productos;
    }

    @Override
    public Producto buscarPorId(int id) {
        String sql = "SELECT " + COLUMNAS + " FROM producto WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al buscar el producto", e);
        }
    }

    @Override
    public Producto crear(Connection conexion, Producto producto) {
        String sql = "INSERT INTO producto "
                + "(categoria_id, nombre, descripcion, precio, imagen, tiempo_preparacion_minutos, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            enlazarCampos(stmt, producto);
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    producto.setId(claves.getInt(1));
                }
            }
            return producto;
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al crear el producto", e);
        }
    }

    @Override
    public void actualizar(Connection conexion, Producto producto) {
        String sql = "UPDATE producto SET categoria_id = ?, nombre = ?, descripcion = ?, "
                + "precio = ?, imagen = ?, tiempo_preparacion_minutos = ?, activo = ? WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            enlazarCampos(stmt, producto);
            stmt.setInt(8, producto.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al actualizar el producto", e);
        }
    }

    @Override
    public void cambiarEstado(int id, boolean activo) {
        String sql = "UPDATE producto SET activo = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setBoolean(1, activo);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al cambiar el estado del producto", e);
        }
    }

    @Override
    public boolean existeNombreEnCategoria(int categoriaId, String nombre, Integer idAExcluir) {
        String sql = "SELECT 1 FROM producto WHERE categoria_id = ? AND nombre = ?"
                + (idAExcluir != null ? " AND id <> ?" : "");
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            stmt.setString(2, nombre);
            if (idAExcluir != null) {
                stmt.setInt(3, idAExcluir);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al verificar el nombre del producto", e);
        }
    }

    private void enlazarCampos(PreparedStatement stmt, Producto producto) throws SQLException {
        stmt.setInt(1, producto.getCategoriaId());
        stmt.setString(2, producto.getNombre());
        stmt.setString(3, producto.getDescripcion());
        stmt.setBigDecimal(4, producto.getPrecio());
        stmt.setString(5, producto.getImagen());
        if (producto.getTiempoPreparacionMinutos() != null) {
            stmt.setInt(6, producto.getTiempoPreparacionMinutos());
        } else {
            stmt.setNull(6, Types.INTEGER);
        }
        stmt.setBoolean(7, Boolean.TRUE.equals(producto.getActivo()));
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        // rs.wasNull() refleja la nulidad de la ULTIMA columna leida, asi
        // que hay que capturarlo justo despues de leer tiempo_preparacion_minutos
        // y antes de leer cualquier otra columna (imagen, activo, etc.);
        // si no, siempre termina reflejando si esa otra columna era nula.
        int minutos = rs.getInt("tiempo_preparacion_minutos");
        boolean minutosEsNulo = rs.wasNull();
        return new Producto(
                rs.getInt("id"),
                rs.getInt("categoria_id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBigDecimal("precio"),
                rs.getString("imagen"),
                minutosEsNulo ? null : minutos,
                rs.getBoolean("activo")
        );
    }
}
