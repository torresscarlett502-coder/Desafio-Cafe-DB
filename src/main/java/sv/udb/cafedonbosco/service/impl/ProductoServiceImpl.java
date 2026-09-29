package sv.udb.cafedonbosco.service.impl;

import sv.udb.cafedonbosco.dao.BitacoraDAO;
import sv.udb.cafedonbosco.dao.CategoriaDAO;
import sv.udb.cafedonbosco.dao.InventarioDAO;
import sv.udb.cafedonbosco.dao.ProductoDAO;
import sv.udb.cafedonbosco.dao.impl.BitacoraDAOImpl;
import sv.udb.cafedonbosco.dao.impl.CategoriaDAOImpl;
import sv.udb.cafedonbosco.dao.impl.InventarioDAOImpl;
import sv.udb.cafedonbosco.dao.impl.ProductoDAOImpl;
import sv.udb.cafedonbosco.dto.request.ProductoRequestDTO;
import sv.udb.cafedonbosco.dto.response.ProductoAdminResponseDTO;
import sv.udb.cafedonbosco.dto.response.ProductoResponseDTO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.exception.RecursoDuplicadoException;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.Bitacora;
import sv.udb.cafedonbosco.model.Categoria;
import sv.udb.cafedonbosco.model.Inventario;
import sv.udb.cafedonbosco.model.Producto;
import sv.udb.cafedonbosco.service.ProductoService;
import sv.udb.cafedonbosco.util.ConexionBD;
import sv.udb.cafedonbosco.util.Constantes;
import sv.udb.cafedonbosco.util.ValidacionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductoServiceImpl implements ProductoService {

    private final ProductoDAO productoDAO;
    private final CategoriaDAO categoriaDAO;
    private final InventarioDAO inventarioDAO;
    private final BitacoraDAO bitacoraDAO;

    public ProductoServiceImpl() {
        this.productoDAO = new ProductoDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
        this.inventarioDAO = new InventarioDAOImpl();
        this.bitacoraDAO = new BitacoraDAOImpl();
    }

    @Override
    public List<ProductoResponseDTO> listarCatalogo(Integer categoriaId, String busqueda, String orden) {
        List<Producto> productos = productoDAO.buscarCatalogo(categoriaId, busqueda, orden);

        Map<Integer, Categoria> categorias = indexarCategorias();
        Map<Integer, Inventario> inventarios = indexarInventarios();

        List<ProductoResponseDTO> catalogo = new ArrayList<>();
        for (Producto producto : productos) {
            catalogo.add(aResponseDTO(producto, categorias, inventarios));
        }
        return catalogo;
    }

    @Override
    public ProductoResponseDTO obtenerDetalle(int id) {
        Producto producto = productoDAO.buscarPorId(id);
        if (producto == null || !Boolean.TRUE.equals(producto.getActivo())) {
            throw new RecursoNoEncontradoException("El producto solicitado no existe.");
        }
        return aResponseDTO(producto, indexarCategorias(), indexarInventarios());
    }

    @Override
    public List<ProductoResponseDTO> listarRelacionados(int productoId, int limite) {
        Producto producto = productoDAO.buscarPorId(productoId);
        if (producto == null) {
            return List.of();
        }
        Map<Integer, Categoria> categorias = indexarCategorias();
        Map<Integer, Inventario> inventarios = indexarInventarios();
        List<ProductoResponseDTO> relacionados = new ArrayList<>();
        for (Producto candidato : productoDAO.listarActivosPorCategoria(producto.getCategoriaId())) {
            if (candidato.getId().equals(productoId)) {
                continue;
            }
            relacionados.add(aResponseDTO(candidato, categorias, inventarios));
            if (relacionados.size() >= limite) {
                break;
            }
        }
        return relacionados;
    }

    @Override
    public List<ProductoAdminResponseDTO> listarAdmin() {
        Map<Integer, Categoria> categorias = indexarCategorias();
        Map<Integer, Inventario> inventarios = indexarInventarios();
        List<ProductoAdminResponseDTO> resultado = new ArrayList<>();
        for (Producto producto : productoDAO.listarTodos()) {
            resultado.add(aAdminDTO(producto, categorias, inventarios));
        }
        return resultado;
    }

    @Override
    public ProductoAdminResponseDTO obtenerDetalleAdmin(int id) {
        Producto producto = productoDAO.buscarPorId(id);
        if (producto == null) {
            throw new RecursoNoEncontradoException("El producto solicitado no existe.");
        }
        return aAdminDTO(producto, indexarCategorias(), indexarInventarios());
    }

    @Override
    public ProductoAdminResponseDTO crear(ProductoRequestDTO datos, int usuarioAdminId) {
        validar(datos);
        Categoria categoria = categoriaDAO.buscarPorId(datos.getCategoriaId());
        if (categoria == null) {
            throw new ValidacionException("La categoria indicada no existe.");
        }
        if (!Boolean.TRUE.equals(categoria.getActivo())) {
            throw new ValidacionException("No se pueden crear productos en una categoria inactiva.");
        }
        if (productoDAO.existeNombreEnCategoria(datos.getCategoriaId(), datos.getNombre().trim(), null)) {
            throw new RecursoDuplicadoException(
                    "Ya existe un producto llamado \"" + datos.getNombre().trim() + "\" en esa categoria.");
        }

        Producto producto = new Producto();
        producto.setCategoriaId(datos.getCategoriaId());
        producto.setNombre(datos.getNombre().trim());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setImagen(datos.getImagen());
        producto.setTiempoPreparacionMinutos(datos.getTiempoPreparacionMinutos());
        producto.setActivo(datos.getActivo() == null || datos.getActivo());

        int stockInicial = datos.getStockInicial() == null ? 0 : datos.getStockInicial();
        int stockMinimo = datos.getStockMinimo() == null ? Constantes.STOCK_MINIMO_POR_DEFECTO : datos.getStockMinimo();

        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();
            conexion.setAutoCommit(false);

            productoDAO.crear(conexion, producto);

            Inventario inventario = new Inventario(null, producto.getId(), stockInicial, stockMinimo);
            inventarioDAO.crear(conexion, inventario);

            bitacoraDAO.registrar(conexion, new Bitacora(usuarioAdminId, "CREAR_PRODUCTO", "PRODUCTO",
                    producto.getId(), "Producto \"" + producto.getNombre() + "\" creado con stock inicial " + stockInicial));

            conexion.commit();
        } catch (SQLException e) {
            revertir(conexion);
            throw new ErrorInternoException("Error al crear el producto con su inventario inicial", e);
        } catch (RuntimeException e) {
            revertir(conexion);
            throw e;
        } finally {
            cerrar(conexion);
        }

        return aAdminDTO(producto, indexarCategorias(), indexarInventarios());
    }

    @Override
    public ProductoAdminResponseDTO actualizar(int id, ProductoRequestDTO datos, int usuarioAdminId) {
        validar(datos);
        Producto existente = productoDAO.buscarPorId(id);
        if (existente == null) {
            throw new RecursoNoEncontradoException("El producto solicitado no existe.");
        }
        Categoria categoria = categoriaDAO.buscarPorId(datos.getCategoriaId());
        if (categoria == null) {
            throw new ValidacionException("La categoria indicada no existe.");
        }
        if (!Boolean.TRUE.equals(categoria.getActivo())) {
            throw new ValidacionException("No se puede mover el producto a una categoria inactiva.");
        }
        if (productoDAO.existeNombreEnCategoria(datos.getCategoriaId(), datos.getNombre().trim(), id)) {
            throw new RecursoDuplicadoException(
                    "Ya existe otro producto llamado \"" + datos.getNombre().trim() + "\" en esa categoria.");
        }

        existente.setCategoriaId(datos.getCategoriaId());
        existente.setNombre(datos.getNombre().trim());
        existente.setDescripcion(datos.getDescripcion());
        existente.setPrecio(datos.getPrecio());
        existente.setImagen(datos.getImagen());
        existente.setTiempoPreparacionMinutos(datos.getTiempoPreparacionMinutos());
        existente.setActivo(datos.getActivo() == null ? existente.getActivo() : datos.getActivo());

        // Producto + stock minimo se guardan juntos en una sola transaccion:
        // dos conexiones separadas podian dejar uno de los dos cambios a
        // medias si el segundo fallaba.
        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();
            conexion.setAutoCommit(false);

            productoDAO.actualizar(conexion, existente);
            if (datos.getStockMinimo() != null) {
                inventarioDAO.actualizarStockMinimo(conexion, id, datos.getStockMinimo());
            }

            bitacoraDAO.registrar(conexion, new Bitacora(usuarioAdminId, "ACTUALIZAR_PRODUCTO", "PRODUCTO",
                    id, "Producto \"" + existente.getNombre() + "\" actualizado"));

            conexion.commit();
        } catch (SQLException e) {
            revertir(conexion);
            throw new ErrorInternoException("Error al actualizar el producto y su inventario", e);
        } catch (RuntimeException e) {
            revertir(conexion);
            throw e;
        } finally {
            cerrar(conexion);
        }

        return aAdminDTO(existente, indexarCategorias(), indexarInventarios());
    }

    @Override
    public void cambiarEstado(int id, boolean activo, int usuarioAdminId) {
        Producto producto = productoDAO.buscarPorId(id);
        if (producto == null) {
            throw new RecursoNoEncontradoException("El producto solicitado no existe.");
        }
        productoDAO.cambiarEstado(id, activo);

        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();
            bitacoraDAO.registrar(conexion, new Bitacora(usuarioAdminId,
                    activo ? "ACTIVAR_PRODUCTO" : "DESACTIVAR_PRODUCTO", "PRODUCTO", id,
                    "Producto \"" + producto.getNombre() + "\" " + (activo ? "activado" : "desactivado")));
        } catch (SQLException e) {
            throw new ErrorInternoException("Error al registrar en la bitacora el cambio de estado del producto", e);
        } finally {
            cerrar(conexion);
        }
    }

    private void validar(ProductoRequestDTO datos) {
        if (datos == null
                || datos.getCategoriaId() == null
                || !ValidacionUtil.esTextoValido(datos.getNombre(), 120)
                || datos.getPrecio() == null
                || datos.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("Nombre, categoria y precio (mayor a cero) son obligatorios.");
        }
        if (datos.getStockInicial() != null && datos.getStockInicial() < 0) {
            throw new ValidacionException("El stock inicial no puede ser negativo.");
        }
        if (datos.getStockMinimo() != null && datos.getStockMinimo() < 0) {
            throw new ValidacionException("El stock minimo no puede ser negativo.");
        }
        if (datos.getTiempoPreparacionMinutos() != null && datos.getTiempoPreparacionMinutos() <= 0) {
            throw new ValidacionException("El tiempo de preparacion debe ser mayor a cero minutos.");
        }
    }

    private Map<Integer, Categoria> indexarCategorias() {
        Map<Integer, Categoria> mapa = new HashMap<>();
        for (Categoria categoria : categoriaDAO.listarTodas()) {
            mapa.put(categoria.getId(), categoria);
        }
        return mapa;
    }

    private Map<Integer, Inventario> indexarInventarios() {
        Map<Integer, Inventario> mapa = new HashMap<>();
        for (Inventario inventario : inventarioDAO.listarTodos()) {
            mapa.put(inventario.getProductoId(), inventario);
        }
        return mapa;
    }

    /** "3" -> "3 minutos"; null -> "No especificado" (lo decide la vista). */
    private String formatearTiempoPreparacion(Integer minutos) {
        if (minutos == null) {
            return null;
        }
        return minutos + (minutos == 1 ? " minuto" : " minutos");
    }

    private ProductoResponseDTO aResponseDTO(Producto producto, Map<Integer, Categoria> categorias, Map<Integer, Inventario> inventarios) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setCategoriaId(producto.getCategoriaId());
        Categoria categoria = categorias.get(producto.getCategoriaId());
        dto.setCategoriaNombre(categoria != null ? categoria.getNombre() : null);
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setImagen(producto.getImagen());
        dto.setTiempoPreparacion(formatearTiempoPreparacion(producto.getTiempoPreparacionMinutos()));
        Inventario inventario = inventarios.get(producto.getId());
        dto.setDisponible(inventario != null && inventario.getCantidad() != null && inventario.getCantidad() > 0);
        return dto;
    }

    private ProductoAdminResponseDTO aAdminDTO(Producto producto, Map<Integer, Categoria> categorias, Map<Integer, Inventario> inventarios) {
        ProductoAdminResponseDTO dto = new ProductoAdminResponseDTO();
        dto.setId(producto.getId());
        dto.setCategoriaId(producto.getCategoriaId());
        Categoria categoria = categorias.get(producto.getCategoriaId());
        dto.setCategoriaNombre(categoria != null ? categoria.getNombre() : null);
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setImagen(producto.getImagen());
        dto.setTiempoPreparacionMinutos(producto.getTiempoPreparacionMinutos());
        dto.setTiempoPreparacion(formatearTiempoPreparacion(producto.getTiempoPreparacionMinutos()));
        dto.setActivo(producto.getActivo());
        Inventario inventario = inventarios.get(producto.getId());
        dto.setStock(inventario != null ? inventario.getCantidad() : 0);
        dto.setStockMinimo(inventario != null ? inventario.getStockMinimo() : Constantes.STOCK_MINIMO_POR_DEFECTO);
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
