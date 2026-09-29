package sv.udb.cafedonbosco.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.udb.cafedonbosco.dao.InventarioDAO;
import sv.udb.cafedonbosco.dao.ProductoDAO;
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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoServiceImplTest {

    @Mock
    private ProductoDAO productoDAO;
    @Mock
    private InventarioDAO inventarioDAO;
    @Mock
    private PersonalizacionService personalizacionService;

    private CarritoService carritoService;

    @BeforeEach
    void configurar() {
        carritoService = new CarritoServiceImpl(productoDAO, inventarioDAO, personalizacionService);
    }

    private Producto productoActivo(int id, String nombre, String precio) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal(precio));
        producto.setActivo(true);
        return producto;
    }

    private Inventario inventarioConStock(int productoId, int cantidad) {
        return new Inventario(1, productoId, cantidad, 1);
    }

    @Test
    void agregarProductoConStockSuficienteFunciona() {
        when(productoDAO.buscarPorId(1)).thenReturn(productoActivo(1, "Cafe Latte", "2.50"));
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 10));

        Carrito carrito = new Carrito();
        carritoService.agregarProducto(carrito, 1, 3);

        assertEquals(3, carrito.getItems().get("1|").getCantidad());
    }

    @Test
    void agregarMasUnidadesQueElStockDisponibleSeRechaza() {
        when(productoDAO.buscarPorId(1)).thenReturn(productoActivo(1, "Cafe Latte", "2.50"));
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 2));

        Carrito carrito = new Carrito();
        assertThrows(ValidacionException.class, () -> carritoService.agregarProducto(carrito, 1, 3));
    }

    @Test
    void agregarEnDosVecesQueSumeMasQueElStockSeRechazaEnLaSegunda() {
        // Regresion del bug donde solo se validaba la cantidad del ultimo
        // agregado, no la suma con lo que ya habia en el carrito: con
        // stock=3, agregar 2 y luego 2 mas (4 en total) debia rechazarse,
        // pero antes de la correccion cada llamada se validaba sola contra
        // el stock total y ambas pasaban.
        when(productoDAO.buscarPorId(1)).thenReturn(productoActivo(1, "Cafe Latte", "2.50"));
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 3));

        Carrito carrito = new Carrito();
        carritoService.agregarProducto(carrito, 1, 2);

        assertThrows(ValidacionException.class, () -> carritoService.agregarProducto(carrito, 1, 2));
        assertEquals(2, carrito.getItems().get("1|").getCantidad(), "el segundo agregado rechazado no debio modificar el carrito");
    }

    @Test
    void agregarUnProductoInactivoSeRechaza() {
        Producto inactivo = productoActivo(1, "Descontinuado", "2.50");
        inactivo.setActivo(false);
        when(productoDAO.buscarPorId(1)).thenReturn(inactivo);

        Carrito carrito = new Carrito();
        assertThrows(RecursoNoEncontradoException.class, () -> carritoService.agregarProducto(carrito, 1, 1));
    }

    @Test
    void agregarUnProductoQueNoExisteSeRechaza() {
        when(productoDAO.buscarPorId(99)).thenReturn(null);

        Carrito carrito = new Carrito();
        assertThrows(RecursoNoEncontradoException.class, () -> carritoService.agregarProducto(carrito, 99, 1));
    }

    @Test
    void agregarCantidadCeroONegativaSeRechazaSinConsultarProductos() {
        Carrito carrito = new Carrito();
        assertThrows(ValidacionException.class, () -> carritoService.agregarProducto(carrito, 1, 0));
        assertThrows(ValidacionException.class, () -> carritoService.agregarProducto(carrito, 1, -1));
    }

    @Test
    void agregarMasDeVeinteUnidadesDeUnMismoProductoSeRechaza() {
        // El limite de 20 por producto se revisa antes de consultar el
        // producto o el inventario, asi que no hace falta stubear esos DAOs.
        Carrito carrito = new Carrito();
        assertThrows(ValidacionException.class, () -> carritoService.agregarProducto(carrito, 1, 21));
    }

    @Test
    void obtenerResumenQuitaDelCarritoUnProductoQueSeQuedoSinStock() {
        Producto producto = productoActivo(1, "Cafe Latte", "2.50");
        Carrito carrito = new Carrito();
        carrito.agregarProducto(new CarritoItem(1, "Cafe Latte", new BigDecimal("2.50"), 2, null));

        when(productoDAO.buscarPorId(1)).thenReturn(producto);
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 0));

        CarritoResponseDTO resumen = carritoService.obtenerResumen(carrito);

        assertTrue(resumen.getItems().isEmpty());
        assertTrue(carrito.estaVacio());
    }

    @Test
    void obtenerResumenActualizaElPrecioSiCambioEnLaBaseDeDatos() {
        Carrito carrito = new Carrito();
        carrito.agregarProducto(new CarritoItem(1, "Cafe Latte", new BigDecimal("2.50"), 2, null));

        Producto conPrecioNuevo = productoActivo(1, "Cafe Latte", "3.00");
        when(productoDAO.buscarPorId(1)).thenReturn(conPrecioNuevo);
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 10));

        CarritoResponseDTO resumen = carritoService.obtenerResumen(carrito);

        assertEquals(new BigDecimal("6.00"), resumen.getSubtotal());
    }

    @Test
    void obtenerResumenRecortaLaCantidadSiYaNoCabeEnElStock() {
        Carrito carrito = new Carrito();
        carrito.agregarProducto(new CarritoItem(1, "Cafe Latte", new BigDecimal("2.50"), 5, null));

        when(productoDAO.buscarPorId(1)).thenReturn(productoActivo(1, "Cafe Latte", "2.50"));
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 2));

        carritoService.obtenerResumen(carrito);

        assertEquals(2, carrito.getItems().get("1|").getCantidad());
    }

    @Test
    void agregarProductoSinOpcionesNuncaConsultaPersonalizacionService() {
        when(productoDAO.buscarPorId(1)).thenReturn(productoActivo(1, "Cafe Latte", "2.50"));
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 10));

        Carrito carrito = new Carrito();
        carritoService.agregarProducto(carrito, 1, 1, null);
        carritoService.agregarProducto(carrito, 1, 1, List.of());

        verifyNoInteractions(personalizacionService);
        assertTrue(carrito.getItems().get("1|").getOpciones().isEmpty());
    }

    @Test
    void agregarProductoConOpcionesGuardaLaFotografiaResueltaEnElItem() {
        when(productoDAO.buscarPorId(1)).thenReturn(productoActivo(1, "Cafe Latte", "2.50"));
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 10));
        OpcionSeleccionada leche = new OpcionSeleccionada(5, "Tipo de leche", "Leche de almendra", new BigDecimal("0.50"));
        when(personalizacionService.validarYResolverOpciones(1, List.of(5))).thenReturn(List.of(leche));

        Carrito carrito = new Carrito();
        carritoService.agregarProducto(carrito, 1, 1, List.of(5));

        String claveLinea = carrito.getItems().keySet().iterator().next();
        assertEquals(1, carrito.getItems().get(claveLinea).getOpciones().size());
        assertEquals(new BigDecimal("3.00"), carrito.getItems().get(claveLinea).getSubtotal());
    }

    @Test
    void agregarUnaUnidadMasSinOpcionesNoBorraLaPersonalizacionYaElegidaYCreaOtraLinea() {
        // Regresion del bug original: el boton "+1" del carrito, la tarjeta
        // del catalogo y la vista rapida agregan el mismo producto sin
        // mandar opciones. Antes de esta correccion arquitectonica, ambas
        // llamadas colisionaban en la MISMA linea (clave = productoId) y la
        // segunda borraba la personalizacion de la primera. Ahora cada
        // combinacion producto+personalizacion es su propia linea: deben
        // quedar DOS lineas independientes, cada una con su propia cantidad
        // y sin que ninguna pierda su personalizacion.
        when(productoDAO.buscarPorId(1)).thenReturn(productoActivo(1, "Cafe Latte", "2.50"));
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 10));
        OpcionSeleccionada leche = new OpcionSeleccionada(5, "Tipo de leche", "Leche de almendra", new BigDecimal("0.50"));
        when(personalizacionService.validarYResolverOpciones(1, List.of(5))).thenReturn(List.of(leche));

        Carrito carrito = new Carrito();
        carritoService.agregarProducto(carrito, 1, 1, List.of(5));
        carritoService.agregarProducto(carrito, 1, 1, null);

        assertEquals(2, carrito.getItems().size(), "debieron quedar dos lineas independientes");
        CarritoItem lineaConLeche = carrito.getItems().get("1|5");
        CarritoItem lineaSinOpciones = carrito.getItems().get("1|");
        assertEquals(1, lineaConLeche.getCantidad());
        assertEquals(1, lineaConLeche.getOpciones().size(), "la opcion elegida no debio perderse ni mezclarse con la otra linea");
        assertEquals(new BigDecimal("3.00"), lineaConLeche.getSubtotal());
        assertEquals(1, lineaSinOpciones.getCantidad());
        assertTrue(lineaSinOpciones.getOpciones().isEmpty());
        assertEquals(new BigDecimal("2.50"), lineaSinOpciones.getSubtotal());
    }

    @Test
    void dosPersonalizacionesDistintasDelMismoProductoSonLineasSeparadasPeroComparteStock() {
        // El pedido explicito del usuario: Cafe Latte + Leche de almendra y
        // Cafe Latte + Leche de avena deben ser DOS lineas (no fusionarse
        // en "2x Cafe Latte, Leche de avena"), pero el stock se valida
        // sobre el TOTAL de unidades del producto entre ambas lineas.
        when(productoDAO.buscarPorId(1)).thenReturn(productoActivo(1, "Cafe Latte", "2.50"));
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 2));
        OpcionSeleccionada almendra = new OpcionSeleccionada(5, "Tipo de leche", "Leche de almendra", new BigDecimal("0.50"));
        OpcionSeleccionada avena = new OpcionSeleccionada(6, "Tipo de leche", "Leche de avena", new BigDecimal("0.50"));
        when(personalizacionService.validarYResolverOpciones(1, List.of(5))).thenReturn(List.of(almendra));
        when(personalizacionService.validarYResolverOpciones(1, List.of(6))).thenReturn(List.of(avena));

        Carrito carrito = new Carrito();
        carritoService.agregarProducto(carrito, 1, 1, List.of(5));
        carritoService.agregarProducto(carrito, 1, 1, List.of(6));

        assertEquals(2, carrito.getItems().size());
        assertEquals("Leche de almendra", carrito.getItems().get("1|5").getOpciones().get(0).getNombreOpcion());
        assertEquals("Leche de avena", carrito.getItems().get("1|6").getOpciones().get(0).getNombreOpcion());

        // Stock=2, ya hay 1+1=2 unidades del producto entre ambas lineas:
        // pedir una unidad MAS de cualquiera de las dos (aunque esa linea
        // individualmente solo tenga 1) debe rechazarse por no quedar stock.
        assertThrows(ValidacionException.class, () -> carritoService.agregarProducto(carrito, 1, 1, List.of(5)));
    }

    @Test
    void actualizarCantidadDeUnaLineaValidaStockContraLasDemasLineasDelMismoProducto() {
        when(productoDAO.buscarPorId(1)).thenReturn(productoActivo(1, "Cafe Latte", "2.50"));
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 3));
        OpcionSeleccionada almendra = new OpcionSeleccionada(5, "Tipo de leche", "Leche de almendra", new BigDecimal("0.50"));
        OpcionSeleccionada avena = new OpcionSeleccionada(6, "Tipo de leche", "Leche de avena", new BigDecimal("0.50"));
        when(personalizacionService.validarYResolverOpciones(1, List.of(5))).thenReturn(List.of(almendra));
        when(personalizacionService.validarYResolverOpciones(1, List.of(6))).thenReturn(List.of(avena));

        Carrito carrito = new Carrito();
        carritoService.agregarProducto(carrito, 1, 1, List.of(5));
        carritoService.agregarProducto(carrito, 1, 1, List.of(6));

        // Stock=3, la otra linea ya usa 1: subir esta linea a 3 pediria 4 en total, debe rechazarse.
        assertThrows(ValidacionException.class, () -> carritoService.actualizarCantidad(carrito, "1|5", 3));
        // Subirla a 2 (2+1=3) si cabe exactamente en el stock.
        carritoService.actualizarCantidad(carrito, "1|5", 2);
        assertEquals(2, carrito.getItems().get("1|5").getCantidad());
    }

    @Test
    void eliminarUnaLineaNoAfectaOtrasLineasDelMismoProducto() {
        when(productoDAO.buscarPorId(1)).thenReturn(productoActivo(1, "Cafe Latte", "2.50"));
        when(inventarioDAO.buscarPorProducto(1)).thenReturn(inventarioConStock(1, 10));
        OpcionSeleccionada almendra = new OpcionSeleccionada(5, "Tipo de leche", "Leche de almendra", new BigDecimal("0.50"));
        when(personalizacionService.validarYResolverOpciones(1, List.of(5))).thenReturn(List.of(almendra));

        Carrito carrito = new Carrito();
        carritoService.agregarProducto(carrito, 1, 1, List.of(5));
        carritoService.agregarProducto(carrito, 1, 1, null);

        carritoService.eliminarProducto(carrito, "1|5");

        assertEquals(1, carrito.getItems().size());
        assertTrue(carrito.getItems().containsKey("1|"));
    }
}
