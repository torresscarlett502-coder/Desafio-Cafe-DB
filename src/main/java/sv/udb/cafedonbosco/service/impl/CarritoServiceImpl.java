package sv.udb.cafedonbosco.service.impl;

import sv.udb.cafedonbosco.dao.InventarioDAO;
import sv.udb.cafedonbosco.dao.ProductoDAO;
import sv.udb.cafedonbosco.dao.impl.InventarioDAOImpl;
import sv.udb.cafedonbosco.dao.impl.ProductoDAOImpl;
import sv.udb.cafedonbosco.dto.response.CarritoResponseDTO;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.Carrito;
import sv.udb.cafedonbosco.model.CarritoItem;
import sv.udb.cafedonbosco.model.Inventario;
import sv.udb.cafedonbosco.model.OpcionSeleccionada;
import sv.udb.cafedonbosco.model.Producto;
import sv.udb.cafedonbosco.service.CarritoService;
import sv.udb.cafedonbosco.service.PersonalizacionService;
import sv.udb.cafedonbosco.util.ValidacionUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarritoServiceImpl implements CarritoService {

    private static final int CANTIDAD_MAXIMA_POR_PRODUCTO = 20;

    private final ProductoDAO productoDAO;
    private final InventarioDAO inventarioDAO;
    private final PersonalizacionService personalizacionService;

    public CarritoServiceImpl() {
        this(new ProductoDAOImpl(), new InventarioDAOImpl(), new PersonalizacionServiceImpl());
    }

    /** Permite inyectar DAOs de prueba (Mockito) sin tocar una base de datos real. */
    public CarritoServiceImpl(ProductoDAO productoDAO, InventarioDAO inventarioDAO) {
        this(productoDAO, inventarioDAO, new PersonalizacionServiceImpl());
    }

    public CarritoServiceImpl(ProductoDAO productoDAO, InventarioDAO inventarioDAO, PersonalizacionService personalizacionService) {
        this.productoDAO = productoDAO;
        this.inventarioDAO = inventarioDAO;
        this.personalizacionService = personalizacionService;
    }

    @Override
    public void agregarProducto(Carrito carrito, int productoId, int cantidad) {
        agregarProducto(carrito, productoId, cantidad, null);
    }

    @Override
    public void agregarProducto(Carrito carrito, int productoId, int cantidad, List<Integer> opcionIds) {
        if (!ValidacionUtil.esCantidadValida(cantidad)) {
            throw new ValidacionException("La cantidad debe ser mayor a 0.");
        }
        // Solo se consulta PersonalizacionService cuando el cliente realmente
        // envio opciones: asi un carrito sin personalizacion (todo el flujo
        // JSP existente) no depende de esa capa para nada.
        List<OpcionSeleccionada> opciones = (opcionIds != null && !opcionIds.isEmpty())
                ? personalizacionService.validarYResolverOpciones(productoId, opcionIds)
                : new ArrayList<>();

        // El stock se valida contra el total pedido del PRODUCTO (sumando
        // todas sus lineas, sin importar la personalizacion de cada una),
        // no solo contra la linea que se esta tocando: dos personalizaciones
        // distintas del mismo producto comparten el mismo stock fisico.
        CarritoItem nuevoTentativo = new CarritoItem(productoId, null, BigDecimal.ZERO, cantidad, null, opciones);
        String claveLinea = nuevoTentativo.getClaveLinea();
        int cantidadEnOtrasLineas = sumarCantidadDelProductoEnOtrasLineas(carrito, productoId, claveLinea);
        CarritoItem existente = carrito.getItems().get(claveLinea);
        int cantidadTotalDeseada = cantidadEnOtrasLineas + (existente != null ? existente.getCantidad() : 0) + cantidad;
        Producto producto = validarProductoDisponible(productoId, cantidadTotalDeseada);

        carrito.agregarProducto(new CarritoItem(
                producto.getId(), producto.getNombre(), producto.getPrecio(), cantidad, producto.getImagen(), opciones
        ));
    }

    @Override
    public void actualizarCantidad(Carrito carrito, String claveLinea, int cantidad) {
        CarritoItem item = carrito.getItems().get(claveLinea);
        if (item == null) {
            throw new RecursoNoEncontradoException("El producto no esta en el carrito.");
        }
        int cantidadEnOtrasLineas = sumarCantidadDelProductoEnOtrasLineas(carrito, item.getProductoId(), claveLinea);
        validarProductoDisponible(item.getProductoId(), cantidadEnOtrasLineas + cantidad);
        carrito.actualizarCantidad(claveLinea, cantidad);
    }

    @Override
    public void eliminarProducto(Carrito carrito, String claveLinea) {
        carrito.eliminarProducto(claveLinea);
    }

    /** Suma la cantidad de todas las lineas del mismo producto, EXCLUYENDO la linea indicada (para no contarla dos veces al validar su propio cambio). */
    private int sumarCantidadDelProductoEnOtrasLineas(Carrito carrito, int productoId, String claveLineaExcluida) {
        int total = 0;
        for (CarritoItem item : carrito.getItems().values()) {
            if (item.getProductoId() == productoId && !item.getClaveLinea().equals(claveLineaExcluida)) {
                total += item.getCantidad();
            }
        }
        return total;
    }

    @Override
    public void vaciar(Carrito carrito) {
        carrito.vaciar();
    }

    @Override
    public CarritoResponseDTO obtenerResumen(Carrito carrito) {
        resincronizarCarrito(carrito);
        BigDecimal subtotal = carrito.calcularSubtotal();
        BigDecimal envio = BigDecimal.ZERO;
        return new CarritoResponseDTO(
                new ArrayList<>(carrito.getItems().values()),
                carrito.contarUnidades(),
                subtotal,
                envio,
                subtotal.add(envio)
        );
    }

    /**
     * Antes de mostrar el carrito se vuelve a leer cada producto contra la
     * BD: si dejo de existir o quedo inactivo se quita del carrito, si el
     * precio cambio se actualiza (el precio guardado al agregarlo puede
     * quedar obsoleto) y si la cantidad guardada ya no cabe en el stock
     * disponible se recorta. El checkout siempre vuelve a validar todo
     * esto dentro de su propia transaccion, pero esto evita que el
     * cliente vea un total distinto al que realmente se le cobrara.
     *
     * El stock disponible es por PRODUCTO, no por linea: si el mismo
     * producto tiene varias lineas (personalizaciones distintas), el
     * recorte reparte el stock disponible entre ellas en el orden en que
     * aparecen en el carrito, en vez de dejar que cada linea se recorte
     * de forma independiente contra el stock total (lo que podria sumar
     * mas unidades de las realmente disponibles).
     */
    private void resincronizarCarrito(Carrito carrito) {
        List<String> aEliminar = new ArrayList<>();
        Map<Integer, Integer> disponiblePorProducto = new HashMap<>();
        for (CarritoItem item : carrito.getItems().values()) {
            Producto producto = productoDAO.buscarPorId(item.getProductoId());
            if (producto == null || !Boolean.TRUE.equals(producto.getActivo())) {
                aEliminar.add(item.getClaveLinea());
                continue;
            }
            item.setNombreProducto(producto.getNombre());
            item.setPrecioUnitario(producto.getPrecio());
            item.setImagen(producto.getImagen());

            int disponible = disponiblePorProducto.computeIfAbsent(item.getProductoId(), id -> {
                Inventario inventario = inventarioDAO.buscarPorProducto(id);
                return inventario != null ? inventario.getCantidad() : 0;
            });
            if (disponible <= 0) {
                aEliminar.add(item.getClaveLinea());
            } else {
                int cantidadAjustada = Math.min(item.getCantidad(), disponible);
                item.setCantidad(cantidadAjustada);
                disponiblePorProducto.put(item.getProductoId(), disponible - cantidadAjustada);
            }
        }
        for (String claveLinea : aEliminar) {
            carrito.eliminarProducto(claveLinea);
        }
    }

    private Producto validarProductoDisponible(int productoId, int cantidad) {
        if (!ValidacionUtil.esCantidadValida(cantidad) || cantidad > CANTIDAD_MAXIMA_POR_PRODUCTO) {
            throw new ValidacionException("La cantidad debe estar entre 1 y " + CANTIDAD_MAXIMA_POR_PRODUCTO + ".");
        }
        Producto producto = productoDAO.buscarPorId(productoId);
        if (producto == null || !Boolean.TRUE.equals(producto.getActivo())) {
            throw new RecursoNoEncontradoException("El producto ya no esta disponible.");
        }
        Inventario inventario = inventarioDAO.buscarPorProducto(productoId);
        if (inventario == null || inventario.getCantidad() < cantidad) {
            throw new ValidacionException("Solo hay " + (inventario == null ? 0 : inventario.getCantidad())
                    + " unidades disponibles de " + producto.getNombre() + ".");
        }
        return producto;
    }
}
