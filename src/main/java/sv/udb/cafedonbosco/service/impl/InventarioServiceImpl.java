package sv.udb.cafedonbosco.service.impl;

import sv.udb.cafedonbosco.dao.BitacoraDAO;
import sv.udb.cafedonbosco.dao.InventarioDAO;
import sv.udb.cafedonbosco.dao.ProductoDAO;
import sv.udb.cafedonbosco.dao.UsuarioDAO;
import sv.udb.cafedonbosco.dao.impl.BitacoraDAOImpl;
import sv.udb.cafedonbosco.dao.impl.InventarioDAOImpl;
import sv.udb.cafedonbosco.dao.impl.ProductoDAOImpl;
import sv.udb.cafedonbosco.dao.impl.UsuarioDAOImpl;
import sv.udb.cafedonbosco.dto.request.InventarioAjusteRequestDTO;
import sv.udb.cafedonbosco.dto.response.MovimientoInventarioResponseDTO;
import sv.udb.cafedonbosco.dto.response.ProductoAdminResponseDTO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.Bitacora;
import sv.udb.cafedonbosco.model.MovimientoInventario;
import sv.udb.cafedonbosco.model.Producto;
import sv.udb.cafedonbosco.model.TipoMovimiento;
import sv.udb.cafedonbosco.model.Usuario;
import sv.udb.cafedonbosco.service.InventarioService;
import sv.udb.cafedonbosco.service.ProductoService;
import sv.udb.cafedonbosco.util.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventarioServiceImpl implements InventarioService {

    private final InventarioDAO inventarioDAO;
    private final BitacoraDAO bitacoraDAO;
    private final ProductoDAO productoDAO;
    private final UsuarioDAO usuarioDAO;
    private final ProductoService productoService;

    public InventarioServiceImpl() {
        this.inventarioDAO = new InventarioDAOImpl();
        this.bitacoraDAO = new BitacoraDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
        this.usuarioDAO = new UsuarioDAOImpl();
        this.productoService = new ProductoServiceImpl();
    }

    @Override
    public List<ProductoAdminResponseDTO> listarInventario() {
        return productoService.listarAdmin();
    }

    @Override
    public void ajustar(InventarioAjusteRequestDTO datos, int usuarioAdminId) {
        if (datos == null || datos.getProductoId() == null || datos.getCantidad() == null || datos.getCantidad() < 0) {
            throw new ValidacionException("Debes indicar el producto y una cantidad valida (0 o mas).");
        }
        if (datos.getStockMinimo() != null && datos.getStockMinimo() < 0) {
            throw new ValidacionException("El stock minimo no puede ser negativo.");
        }

        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();
            conexion.setAutoCommit(false);

            if (!inventarioDAO.existeParaProducto(conexion, datos.getProductoId())) {
                throw new RecursoNoEncontradoException("El producto no tiene un registro de inventario.");
            }

            int stockAnterior = inventarioDAO.obtenerCantidadActual(conexion, datos.getProductoId());
            inventarioDAO.fijarCantidad(conexion, datos.getProductoId(), datos.getCantidad());

            if (datos.getCantidad() != stockAnterior) {
                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setProductoId(datos.getProductoId());
                movimiento.setTipoMovimiento(TipoMovimiento.AJUSTE);
                movimiento.setCantidad(Math.abs(datos.getCantidad() - stockAnterior));
                movimiento.setStockAnterior(stockAnterior);
                movimiento.setStockNuevo(datos.getCantidad());
                movimiento.setMotivo("Ajuste manual de inventario");
                movimiento.setUsuarioId(usuarioAdminId);
                inventarioDAO.registrarMovimiento(conexion, movimiento);
            }

            if (datos.getStockMinimo() != null) {
                inventarioDAO.actualizarStockMinimo(conexion, datos.getProductoId(), datos.getStockMinimo());
            }

            bitacoraDAO.registrar(conexion, new Bitacora(usuarioAdminId, "AJUSTAR_INVENTARIO", "PRODUCTO",
                    datos.getProductoId(), "Cantidad fijada en " + datos.getCantidad()
                    + (datos.getStockMinimo() != null ? ", stock minimo en " + datos.getStockMinimo() : "")));

            conexion.commit();
        } catch (SQLException e) {
            revertir(conexion);
            throw new ErrorInternoException("Error al ajustar el inventario", e);
        } catch (RuntimeException e) {
            revertir(conexion);
            throw e;
        } finally {
            cerrar(conexion);
        }
    }

    @Override
    public List<MovimientoInventarioResponseDTO> listarMovimientos(Integer productoId, int limite) {
        List<MovimientoInventarioResponseDTO> resultado = new ArrayList<>();
        for (MovimientoInventario movimiento : inventarioDAO.listarMovimientos(productoId, limite)) {
            resultado.add(aResponseDTO(movimiento));
        }
        return resultado;
    }

    private MovimientoInventarioResponseDTO aResponseDTO(MovimientoInventario movimiento) {
        MovimientoInventarioResponseDTO dto = new MovimientoInventarioResponseDTO();
        dto.setId(movimiento.getId());
        dto.setProductoId(movimiento.getProductoId());
        Producto producto = productoDAO.buscarPorId(movimiento.getProductoId());
        dto.setNombreProducto(producto != null ? producto.getNombre() : null);
        dto.setTipoMovimiento(movimiento.getTipoMovimiento());
        dto.setCantidad(movimiento.getCantidad());
        dto.setStockAnterior(movimiento.getStockAnterior());
        dto.setStockNuevo(movimiento.getStockNuevo());
        dto.setMotivo(movimiento.getMotivo());
        dto.setVentaId(movimiento.getVentaId());
        dto.setCompraId(movimiento.getCompraId());
        dto.setUsuarioId(movimiento.getUsuarioId());
        if (movimiento.getUsuarioId() != null) {
            Usuario usuario = usuarioDAO.buscarPorId(movimiento.getUsuarioId());
            dto.setUsuarioNombre(usuario != null ? usuario.getNombre() : null);
        }
        dto.setFecha(movimiento.getFecha());
        return dto;
    }

    private void revertir(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.rollback();
            } catch (SQLException ignorada) {
                // La conexion se cerrara de todas formas en el bloque finally.
            }
        }
    }

    private void cerrar(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException ignorada) {
                // No hay una accion util adicional si el cierre falla.
            }
        }
    }
}
