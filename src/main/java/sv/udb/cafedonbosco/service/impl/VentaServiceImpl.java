package sv.udb.cafedonbosco.service.impl;

import sv.udb.cafedonbosco.dao.BitacoraDAO;
import sv.udb.cafedonbosco.dao.DetalleVentaOpcionDAO;
import sv.udb.cafedonbosco.dao.InventarioDAO;
import sv.udb.cafedonbosco.dao.ProductoDAO;
import sv.udb.cafedonbosco.dao.VentaDAO;
import sv.udb.cafedonbosco.dao.impl.BitacoraDAOImpl;
import sv.udb.cafedonbosco.dao.impl.DetalleVentaOpcionDAOImpl;
import sv.udb.cafedonbosco.dao.impl.InventarioDAOImpl;
import sv.udb.cafedonbosco.dao.impl.ProductoDAOImpl;
import sv.udb.cafedonbosco.dao.impl.VentaDAOImpl;
import sv.udb.cafedonbosco.dto.request.CarritoItemRequestDTO;
import sv.udb.cafedonbosco.dto.request.CheckoutRequestDTO;
import sv.udb.cafedonbosco.dto.request.VentaFiltroDTO;
import sv.udb.cafedonbosco.dto.request.VentaPresencialRequestDTO;
import sv.udb.cafedonbosco.dto.response.DetalleVentaResponseDTO;
import sv.udb.cafedonbosco.dto.response.OpcionSeleccionadaResponseDTO;
import sv.udb.cafedonbosco.dto.response.VentaResponseDTO;
import sv.udb.cafedonbosco.exception.AccesoDenegadoException;
import sv.udb.cafedonbosco.exception.ErrorInternoException;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.exception.StockInsuficienteException;
import sv.udb.cafedonbosco.exception.TransicionInvalidaException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.Bitacora;
import sv.udb.cafedonbosco.model.Carrito;
import sv.udb.cafedonbosco.model.CarritoItem;
import sv.udb.cafedonbosco.model.DetalleVenta;
import sv.udb.cafedonbosco.model.EstadoPago;
import sv.udb.cafedonbosco.model.EstadoVenta;
import sv.udb.cafedonbosco.model.MovimientoInventario;
import sv.udb.cafedonbosco.model.Producto;
import sv.udb.cafedonbosco.model.TipoMovimiento;
import sv.udb.cafedonbosco.model.TipoVenta;
import sv.udb.cafedonbosco.model.OpcionSeleccionada;
import sv.udb.cafedonbosco.model.Venta;
import sv.udb.cafedonbosco.service.HorarioAtencionService;
import sv.udb.cafedonbosco.service.PersonalizacionService;
import sv.udb.cafedonbosco.service.VentaService;
import sv.udb.cafedonbosco.util.ConexionBD;
import sv.udb.cafedonbosco.util.Constantes;
import sv.udb.cafedonbosco.util.FechaUtil;
import sv.udb.cafedonbosco.util.TransicionEstadoValidator;
import sv.udb.cafedonbosco.util.ValidacionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VentaServiceImpl implements VentaService {

    private static final Set<String> METODOS_PAGO_VALIDOS = Set.of("TARJETA", "TRANSFERENCIA", "CONTRA_ENTREGA", "EFECTIVO");
    private static final int MINUTOS_PREPARACION_POR_DEFECTO = 5;

    private final VentaDAO ventaDAO;
    private final ProductoDAO productoDAO;
    private final InventarioDAO inventarioDAO;
    private final BitacoraDAO bitacoraDAO;
    private final HorarioAtencionService horarioAtencionService;
    private final PersonalizacionService personalizacionService;
    private final DetalleVentaOpcionDAO detalleVentaOpcionDAO;

    public VentaServiceImpl() {
        this.ventaDAO = new VentaDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
        this.inventarioDAO = new InventarioDAOImpl();
        this.bitacoraDAO = new BitacoraDAOImpl();
        this.horarioAtencionService = new HorarioAtencionServiceImpl();
        this.personalizacionService = new PersonalizacionServiceImpl();
        this.detalleVentaOpcionDAO = new DetalleVentaOpcionDAOImpl();
    }

    @Override
    public VentaResponseDTO procesarCheckoutWeb(Carrito carrito, CheckoutRequestDTO datos, Integer usuarioId, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Venta yaProcesada = ventaDAO.buscarPorIdempotencyKey(idempotencyKey);
            if (yaProcesada != null) {
                return aResponseDTO(yaProcesada);
            }
        }
        horarioAtencionService.validarLocalAbiertoParaPedido(TipoVenta.WEB);
        if (carrito == null || carrito.estaVacio()) {
            throw new ValidacionException("El carrito esta vacio.");
        }
        validarDatosCheckout(datos);

        Venta venta = new Venta();
        venta.setUsuarioId(usuarioId);
        venta.setTipoVenta(TipoVenta.WEB);
        venta.setEstado(EstadoVenta.RECIBIDO);
        venta.setMetodoPago(datos.getMetodoPago());
        // PAGO SIMULADO: este proyecto NO tiene integracion real con ningun
        // gateway de pago (Stripe, PayPal, Mercado Pago, un banco, etc.).
        // No existe intento de pago, autorizacion, 3DS, webhook ni id de
        // transaccion real: elegir "TARJETA" solo marca el pago como
        // aprobado de inmediato para poder probar el flujo completo del
        // sistema. NUNCA se solicitan ni se guardan datos de tarjeta.
        // El frontend (checkout.jsp) muestra un aviso explicito de que
        // esto es una simulacion. Antes de usar este sistema con pagos
        // reales hay que reemplazar esta linea por una integracion real:
        // Checkout -> crear intento de pago -> gateway -> webhook de
        // confirmacion -> recien ahi cambiar EstadoPago a APROBADO (nunca
        // confiar en la sola eleccion del metodo de pago para aprobar).
        venta.setEstadoPago("TARJETA".equals(datos.getMetodoPago()) ? EstadoPago.APROBADO : EstadoPago.PENDIENTE);
        venta.setTipoEntrega(datos.getTipoEntrega());
        venta.setNombreCliente(datos.getNombreCompleto().trim());
        venta.setCorreoCliente(datos.getCorreo().trim().toLowerCase());
        venta.setTelefonoCliente(datos.getTelefono().trim());
        venta.setDireccionCliente(datos.getDireccion());
        venta.setNotas(datos.getNotas());
        venta.setIdempotencyKey(idempotencyKey);

        List<DetalleVenta> detalles = new ArrayList<>();
        for (CarritoItem item : carrito.getItems().values()) {
            DetalleVenta detalle = new DetalleVenta(item.getProductoId(), item.getNombreProducto(), item.getCantidad(), null, null);
            detalle.setOpcionIdsSolicitados(extraerOpcionIds(item.getOpciones()));
            detalles.add(detalle);
        }

        BigDecimal envio = Constantes.ENTREGA_DOMICILIO.equalsIgnoreCase(datos.getTipoEntrega())
                ? Constantes.TARIFA_ENVIO_DOMICILIO
                : BigDecimal.ZERO;

        Venta registrada = registrarConTransaccion(venta, detalles, envio, usuarioId);
        carrito.vaciar();
        return aResponseDTO(registrada);
    }

    @Override
    public VentaResponseDTO registrarVentaPresencial(VentaPresencialRequestDTO datos, int usuarioAdminId) {
        horarioAtencionService.validarLocalAbiertoParaPedido(TipoVenta.PRESENCIAL);
        if (datos == null || datos.getItems() == null || datos.getItems().isEmpty()) {
            throw new ValidacionException("La venta debe incluir al menos un producto.");
        }
        String metodoPago = datos.getMetodoPago() == null ? "EFECTIVO" : datos.getMetodoPago().toUpperCase();
        if (!METODOS_PAGO_VALIDOS.contains(metodoPago)) {
            throw new ValidacionException("Metodo de pago no valido.");
        }

        Venta venta = new Venta();
        venta.setUsuarioId(usuarioAdminId);
        venta.setTipoVenta(TipoVenta.PRESENCIAL);
        // Se sirve al momento en el mostrador: nace ya en un estado terminal
        // en vez de recorrer RECIBIDO->EN_PREPARACION->LISTO como un pedido
        // web, porque no hay una cola de preparacion separada del cliente
        // esperando en el mostrador.
        venta.setEstado(EstadoVenta.ENTREGADO);
        venta.setMetodoPago(metodoPago);
        venta.setEstadoPago(EstadoPago.APROBADO);
        venta.setTipoEntrega(Constantes.ENTREGA_RECOGER);

        List<DetalleVenta> detalles = new ArrayList<>();
        for (CarritoItemRequestDTO item : datos.getItems()) {
            if (!ValidacionUtil.esCantidadValida(item.getCantidad()) || item.getProductoId() == null) {
                throw new ValidacionException("Cada producto de la venta necesita un id y una cantidad valida.");
            }
            DetalleVenta detalle = new DetalleVenta(item.getProductoId(), null, item.getCantidad(), null, null);
            detalle.setOpcionIdsSolicitados(item.getOpcionIds());
            detalles.add(detalle);
        }

        Venta registrada = registrarConTransaccion(venta, detalles, BigDecimal.ZERO, usuarioAdminId);
        return aResponseDTO(registrada);
    }

    /**
     * Nucleo transaccional compartido por la venta web y la presencial:
     * vuelve a leer el precio y el stock vigentes de cada producto (nunca
     * se confia en lo que traiga la sesion o la solicitud), descuenta el
     * inventario de forma atomica, deja un movimiento de inventario por
     * cada linea y registra la cabecera y el detalle en una unica
     * transaccion JDBC.
     */
    private Venta registrarConTransaccion(Venta venta, List<DetalleVenta> detallesSolicitados, BigDecimal envio, Integer usuarioId) {
        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();
            conexion.setAutoCommit(false);

            BigDecimal subtotal = BigDecimal.ZERO;
            List<DetalleVenta> detallesFinales = new ArrayList<>();
            List<MovimientoInventario> movimientos = new ArrayList<>();

            for (DetalleVenta solicitado : detallesSolicitados) {
                Producto producto = productoDAO.buscarPorId(solicitado.getProductoId());
                if (producto == null || !Boolean.TRUE.equals(producto.getActivo())) {
                    throw new RecursoNoEncontradoException("Uno de los productos ya no esta disponible.");
                }

                boolean descontado = inventarioDAO.descontarStock(conexion, producto.getId(), solicitado.getCantidad());
                if (!descontado) {
                    throw new StockInsuficienteException(producto.getNombre());
                }
                int stockNuevo = inventarioDAO.obtenerCantidadActual(conexion, producto.getId());

                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setProductoId(producto.getId());
                movimiento.setTipoMovimiento(TipoMovimiento.SALIDA);
                movimiento.setCantidad(solicitado.getCantidad());
                movimiento.setStockAnterior(stockNuevo + solicitado.getCantidad());
                movimiento.setStockNuevo(stockNuevo);
                movimiento.setUsuarioId(usuarioId);
                movimientos.add(movimiento);

                // Las opciones tambien se vuelven a resolver aqui (nunca se
                // confia en lo que traiga el carrito/la solicitud): si una
                // opcion se desactivo o cambio de precio entre que se agrego
                // al carrito y el checkout, esto usa el estado vigente.
                List<OpcionSeleccionada> opciones = personalizacionService.validarYResolverOpciones(
                        producto.getId(), solicitado.getOpcionIdsSolicitados());
                BigDecimal precioAdicionalOpciones = BigDecimal.ZERO;
                for (OpcionSeleccionada opcion : opciones) {
                    precioAdicionalOpciones = precioAdicionalOpciones.add(opcion.getPrecioAdicional());
                }

                BigDecimal precioVigente = producto.getPrecio().add(precioAdicionalOpciones);
                BigDecimal subtotalLinea = precioVigente.multiply(BigDecimal.valueOf(solicitado.getCantidad()));
                subtotal = subtotal.add(subtotalLinea);

                DetalleVenta detalleFinal = new DetalleVenta(
                        producto.getId(), producto.getNombre(), solicitado.getCantidad(), precioVigente, subtotalLinea
                );
                detalleFinal.setOpciones(opciones);
                detallesFinales.add(detalleFinal);
            }

            venta.setSubtotal(subtotal);
            venta.setEnvio(envio);
            venta.setTotal(subtotal.add(envio));
            venta.setTokenTicket(generarTokenTicket());
            // Se fija explicitamente en vez de confiar en el DEFAULT
            // CURRENT_TIMESTAMP de la columna: ese default usa la zona
            // horaria del propio servidor de MySQL, que puede no ser
            // El Salvador si la base corre en un contenedor/nube en UTC.
            venta.setFecha(FechaUtil.obtenerFechaHoraActual());

            ventaDAO.crear(conexion, venta);

            for (int i = 0; i < detallesFinales.size(); i++) {
                DetalleVenta detalle = detallesFinales.get(i);
                ventaDAO.crearDetalle(conexion, detalle, venta.getId());
                for (OpcionSeleccionada opcion : detalle.getOpciones()) {
                    detalleVentaOpcionDAO.registrar(conexion, detalle.getId(), opcion);
                }

                MovimientoInventario movimiento = movimientos.get(i);
                movimiento.setVentaId(venta.getId());
                movimiento.setMotivo("Venta #" + venta.getId());
                inventarioDAO.registrarMovimiento(conexion, movimiento);
            }
            venta.setDetalles(detallesFinales);

            if (venta.getEstado() == EstadoVenta.ENTREGADO) {
                LocalDateTime ahora = FechaUtil.obtenerFechaHoraActual();
                ventaDAO.marcarEntregado(conexion, venta.getId(), ahora);
                venta.setFechaEntregado(ahora);
            }

            conexion.commit();
            return venta;
        } catch (SQLException e) {
            revertir(conexion);
            Venta yaCreadaPorOtraSolicitud = recuperarPorIdempotenciaSiAplica(venta, e);
            if (yaCreadaPorOtraSolicitud != null) {
                return yaCreadaPorOtraSolicitud;
            }
            throw new ErrorInternoException("Error al registrar la venta", e);
        } catch (RuntimeException e) {
            revertir(conexion);
            // Las llamadas a ventaDAO/inventarioDAO/productoDAO ya envuelven
            // cualquier SQLException en una excepcion sin marcar (p. ej.
            // ErrorInternoException) antes de que llegue hasta aqui, asi que
            // en la practica una violacion de unicidad del idempotency_key
            // (dos solicitudes concurrentes con la misma clave) se ve como
            // un RuntimeException, no como el SQLException de mas arriba.
            // Por eso la recuperacion se intenta en ambos catch.
            Venta yaCreadaPorOtraSolicitud = recuperarPorIdempotenciaSiAplica(venta, e);
            if (yaCreadaPorOtraSolicitud != null) {
                return yaCreadaPorOtraSolicitud;
            }
            throw e;
        } finally {
            cerrar(conexion);
        }
    }

    /**
     * Si la venta llevaba una clave de idempotencia y el fallo fue por esa
     * clave duplicada (otra solicitud concurrente ya la registro primero),
     * devuelve esa venta ya creada en vez de fallar. Recorre toda la
     * cadena de causas porque el error real puede llegar envuelto en mas
     * de una excepcion.
     */
    private Venta recuperarPorIdempotenciaSiAplica(Venta venta, Throwable error) {
        if (venta.getIdempotencyKey() == null || !esViolacionDeUnicidad(error)) {
            return null;
        }
        return ventaDAO.buscarPorIdempotencyKey(venta.getIdempotencyKey());
    }

    private boolean esViolacionDeUnicidad(Throwable error) {
        for (Throwable actual = error; actual != null; actual = actual.getCause()) {
            if (actual instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
        }
        return false;
    }

    @Override
    public VentaResponseDTO obtenerPorToken(String token) {
        if (!ValidacionUtil.esTextoValido(token)) {
            throw new RecursoNoEncontradoException("Ticket no encontrado.");
        }
        Venta venta = ventaDAO.buscarPorToken(token);
        if (venta == null) {
            throw new RecursoNoEncontradoException("Ticket no encontrado.");
        }
        return aResponseDTO(venta);
    }

    @Override
    public VentaResponseDTO obtenerPorId(int id) {
        Venta venta = ventaDAO.buscarPorId(id);
        if (venta == null) {
            throw new RecursoNoEncontradoException("La venta solicitada no existe.");
        }
        return aResponseDTO(venta);
    }

    @Override
    public List<VentaResponseDTO> listarHistorial(TipoVenta tipoVenta, int limite) {
        List<VentaResponseDTO> resultado = new ArrayList<>();
        for (Venta venta : ventaDAO.listarHistorial(tipoVenta, limite)) {
            VentaResponseDTO dto = aResponseDTO(venta);
            dto.setTokenTicket(null);
            resultado.add(dto);
        }
        return resultado;
    }

    @Override
    public List<VentaResponseDTO> listarFiltrado(VentaFiltroDTO filtro) {
        List<VentaResponseDTO> resultado = new ArrayList<>();
        for (Venta venta : ventaDAO.listarFiltrado(filtro)) {
            VentaResponseDTO dto = aResponseDTO(venta);
            dto.setTokenTicket(null);
            resultado.add(dto);
        }
        return resultado;
    }

    @Override
    public List<VentaResponseDTO> listarPedidosDeUsuario(int usuarioId, int limite) {
        List<VentaResponseDTO> resultado = new ArrayList<>();
        for (Venta venta : ventaDAO.listarPorUsuario(usuarioId, limite)) {
            resultado.add(aResponseDTO(venta));
        }
        return resultado;
    }

    @Override
    public VentaResponseDTO obtenerPedidoDeUsuario(int usuarioId, int ventaId) {
        Venta venta = ventaDAO.buscarPorId(ventaId);
        if (venta == null) {
            throw new RecursoNoEncontradoException("El pedido solicitado no existe.");
        }
        if (venta.getUsuarioId() == null || !venta.getUsuarioId().equals(usuarioId)) {
            throw new AccesoDenegadoException("No tienes permiso para ver este pedido.");
        }
        return aResponseDTO(venta);
    }

    @Override
    public VentaResponseDTO cambiarEstado(int ventaId, EstadoVenta nuevoEstado, int usuarioAdminId) {
        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();
            conexion.setAutoCommit(false);

            Venta venta = ventaDAO.buscarPorIdParaActualizar(conexion, ventaId);
            if (venta == null) {
                throw new RecursoNoEncontradoException("La venta solicitada no existe.");
            }
            TransicionEstadoValidator.validar(venta.getEstado(), nuevoEstado);

            LocalDateTime ahora = FechaUtil.obtenerFechaHoraActual();
            switch (nuevoEstado) {
                case EN_PREPARACION -> {
                    int minutos = calcularMinutosPreparacion(venta);
                    LocalDateTime estimada = ahora.plusMinutes(minutos);
                    ventaDAO.iniciarPreparacion(conexion, ventaId, ahora, estimada);
                    venta.setFechaInicioPreparacion(ahora);
                    venta.setFechaEstimadaListo(estimada);
                }
                case LISTO -> {
                    ventaDAO.marcarListo(conexion, ventaId, ahora);
                    venta.setFechaListo(ahora);
                }
                case ENTREGADO -> {
                    ventaDAO.marcarEntregado(conexion, ventaId, ahora);
                    venta.setFechaEntregado(ahora);
                }
                case CANCELADO -> devolverStockYCancelar(conexion, venta, usuarioAdminId);
                default -> throw new TransicionInvalidaException("Transicion no soportada: " + nuevoEstado);
            }
            venta.setEstado(nuevoEstado);

            bitacoraDAO.registrar(conexion, new Bitacora(usuarioAdminId, "CAMBIAR_ESTADO_VENTA", "VENTA", ventaId,
                    "Estado cambiado a " + nuevoEstado));

            conexion.commit();
            return aResponseDTO(venta);
        } catch (SQLException e) {
            revertir(conexion);
            throw new ErrorInternoException("Error al cambiar el estado de la venta", e);
        } catch (RuntimeException e) {
            revertir(conexion);
            throw e;
        } finally {
            cerrar(conexion);
        }
    }

    @Override
    public VentaResponseDTO cambiarEstadoPago(int ventaId, EstadoPago nuevoEstadoPago, int usuarioAdminId) {
        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();
            conexion.setAutoCommit(false);

            Venta venta = ventaDAO.buscarPorIdParaActualizar(conexion, ventaId);
            if (venta == null) {
                throw new RecursoNoEncontradoException("La venta solicitada no existe.");
            }
            if (venta.getEstadoPago() == nuevoEstadoPago) {
                throw new TransicionInvalidaException("El pago ya esta en estado " + nuevoEstadoPago + ".");
            }

            ventaDAO.actualizarEstadoPago(conexion, ventaId, nuevoEstadoPago);
            venta.setEstadoPago(nuevoEstadoPago);

            bitacoraDAO.registrar(conexion, new Bitacora(usuarioAdminId, "CAMBIAR_ESTADO_PAGO", "VENTA", ventaId,
                    "Pago cambiado a " + nuevoEstadoPago));

            conexion.commit();
            return aResponseDTO(venta);
        } catch (SQLException e) {
            revertir(conexion);
            throw new ErrorInternoException("Error al cambiar el estado de pago", e);
        } catch (RuntimeException e) {
            revertir(conexion);
            throw e;
        } finally {
            cerrar(conexion);
        }
    }

    @Override
    public VentaResponseDTO cancelar(int ventaId, int usuarioAdminId) {
        return cambiarEstado(ventaId, EstadoVenta.CANCELADO, usuarioAdminId);
    }

    /**
     * Devuelve al inventario cada linea de la venta y deja un movimiento
     * DEVOLUCION por producto. Se llama dentro de la transaccion de
     * cambiarEstado, con la venta ya bloqueada por SELECT ... FOR UPDATE,
     * asi que no hay forma de que esto se ejecute dos veces para la misma
     * venta (la segunda solicitud vera estado = CANCELADO y
     * TransicionEstadoValidator la rechazara antes de llegar aqui).
     */
    private void devolverStockYCancelar(Connection conexion, Venta venta, int usuarioAdminId) {
        for (DetalleVenta detalle : venta.getDetalles()) {
            inventarioDAO.incrementarStock(conexion, detalle.getProductoId(), detalle.getCantidad());
            int stockNuevo = inventarioDAO.obtenerCantidadActual(conexion, detalle.getProductoId());

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProductoId(detalle.getProductoId());
            movimiento.setTipoMovimiento(TipoMovimiento.DEVOLUCION);
            movimiento.setCantidad(detalle.getCantidad());
            movimiento.setStockAnterior(stockNuevo - detalle.getCantidad());
            movimiento.setStockNuevo(stockNuevo);
            movimiento.setMotivo("Cancelacion de la venta #" + venta.getId());
            movimiento.setVentaId(venta.getId());
            movimiento.setUsuarioId(usuarioAdminId);
            inventarioDAO.registrarMovimiento(conexion, movimiento);
        }
        ventaDAO.marcarCancelado(conexion, venta.getId());
    }

    @Override
    public int procesarPreparacionesVencidas() {
        int actualizadas = 0;
        for (Venta venta : ventaDAO.listarEnPreparacionVencidas()) {
            Connection conexion = null;
            try {
                conexion = ConexionBD.obtenerConexion();
                conexion.setAutoCommit(false);

                // Se vuelve a comprobar bajo bloqueo por si un administrador
                // ya movio manualmente esta venta entre el listado inicial
                // y este punto.
                Venta bloqueada = ventaDAO.buscarPorIdParaActualizar(conexion, venta.getId());
                if (bloqueada != null && bloqueada.getEstado() == EstadoVenta.EN_PREPARACION) {
                    ventaDAO.marcarListo(conexion, venta.getId(), FechaUtil.obtenerFechaHoraActual());
                    conexion.commit();
                    actualizadas++;
                } else {
                    conexion.rollback();
                }
            } catch (SQLException e) {
                revertir(conexion);
                // Un fallo en una venta no debe detener el resto del barrido del scheduler.
            } finally {
                cerrar(conexion);
            }
        }
        return actualizadas;
    }

    private int calcularMinutosPreparacion(Venta venta) {
        int maximo = 0;
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = productoDAO.buscarPorId(detalle.getProductoId());
            if (producto != null && producto.getTiempoPreparacionMinutos() != null) {
                maximo = Math.max(maximo, producto.getTiempoPreparacionMinutos());
            }
        }
        return maximo > 0 ? maximo : MINUTOS_PREPARACION_POR_DEFECTO;
    }

    private void validarDatosCheckout(CheckoutRequestDTO datos) {
        if (datos == null || !ValidacionUtil.esTextoValido(datos.getNombreCompleto(), 150)) {
            throw new ValidacionException("El nombre completo es obligatorio.");
        }
        if (!ValidacionUtil.esCorreoValido(datos.getCorreo())) {
            throw new ValidacionException("El correo electronico no es valido.");
        }
        if (!ValidacionUtil.esTelefonoValido(datos.getTelefono())) {
            throw new ValidacionException("El telefono no es valido.");
        }
        if (!Constantes.ENTREGA_RECOGER.equalsIgnoreCase(datos.getTipoEntrega())
                && !Constantes.ENTREGA_DOMICILIO.equalsIgnoreCase(datos.getTipoEntrega())) {
            throw new ValidacionException("El tipo de entrega debe ser RECOGER o DOMICILIO.");
        }
        if (Constantes.ENTREGA_DOMICILIO.equalsIgnoreCase(datos.getTipoEntrega())
                && !ValidacionUtil.esTextoValido(datos.getDireccion(), 255)) {
            throw new ValidacionException("La direccion es obligatoria para la entrega a domicilio.");
        }
        if (datos.getMetodoPago() == null || !METODOS_PAGO_VALIDOS.contains(datos.getMetodoPago().toUpperCase())) {
            throw new ValidacionException("Selecciona un metodo de pago valido.");
        }
        datos.setTipoEntrega(datos.getTipoEntrega().toUpperCase());
        datos.setMetodoPago(datos.getMetodoPago().toUpperCase());
    }

    private String generarTokenTicket() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private List<Integer> extraerOpcionIds(List<OpcionSeleccionada> opciones) {
        List<Integer> ids = new ArrayList<>();
        for (OpcionSeleccionada opcion : opciones) {
            ids.add(opcion.getOpcionId());
        }
        return ids;
    }

    private VentaResponseDTO aResponseDTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(venta.getId());
        dto.setTipoVenta(venta.getTipoVenta());
        dto.setEstado(venta.getEstado());
        dto.setSubtotal(venta.getSubtotal());
        dto.setEnvio(venta.getEnvio());
        dto.setTotal(venta.getTotal());
        dto.setMetodoPago(venta.getMetodoPago());
        dto.setEstadoPago(venta.getEstadoPago());
        dto.setTipoEntrega(venta.getTipoEntrega());
        dto.setNombreCliente(venta.getNombreCliente());
        dto.setCorreoCliente(venta.getCorreoCliente());
        dto.setTelefonoCliente(venta.getTelefonoCliente());
        dto.setDireccionCliente(venta.getDireccionCliente());
        dto.setNotas(venta.getNotas());
        dto.setFecha(venta.getFecha());
        dto.setFechaInicioPreparacion(venta.getFechaInicioPreparacion());
        dto.setFechaEstimadaListo(venta.getFechaEstimadaListo());
        dto.setFechaListo(venta.getFechaListo());
        dto.setFechaEntregado(venta.getFechaEntregado());
        dto.setTokenTicket(venta.getTokenTicket());

        List<DetalleVentaResponseDTO> detalles = new ArrayList<>();
        for (DetalleVenta detalle : venta.getDetalles()) {
            DetalleVentaResponseDTO detalleDTO = new DetalleVentaResponseDTO(
                    detalle.getProductoId(), detalle.getNombreProducto(), detalle.getCantidad(),
                    detalle.getPrecioUnitario(), detalle.getSubtotal()
            );
            List<OpcionSeleccionadaResponseDTO> opciones = new ArrayList<>();
            for (OpcionSeleccionada opcion : detalle.getOpciones()) {
                opciones.add(new OpcionSeleccionadaResponseDTO(
                        opcion.getOpcionId(), opcion.getNombreGrupo(), opcion.getNombreOpcion(), opcion.getPrecioAdicional()
                ));
            }
            detalleDTO.setOpciones(opciones);
            detalles.add(detalleDTO);
        }
        dto.setDetalles(detalles);
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
