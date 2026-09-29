package sv.udb.cafedonbosco.service;

import sv.udb.cafedonbosco.dto.request.CheckoutRequestDTO;
import sv.udb.cafedonbosco.dto.request.VentaFiltroDTO;
import sv.udb.cafedonbosco.dto.request.VentaPresencialRequestDTO;
import sv.udb.cafedonbosco.dto.response.VentaResponseDTO;
import sv.udb.cafedonbosco.model.Carrito;
import sv.udb.cafedonbosco.model.EstadoPago;
import sv.udb.cafedonbosco.model.EstadoVenta;
import sv.udb.cafedonbosco.model.TipoVenta;

import java.util.List;

public interface VentaService {

    /**
     * Valida el carrito y los datos de checkout, revalida precios y stock
     * contra la base de datos dentro de una transaccion, registra la venta
     * WEB con sus detalles, descuenta el inventario y vacia el carrito.
     * idempotencyKey (opcional) evita procesar dos veces un doble envio
     * del mismo formulario: si ya existe una venta con esa clave, se
     * devuelve esa misma venta en vez de crear una nueva.
     */
    VentaResponseDTO procesarCheckoutWeb(Carrito carrito, CheckoutRequestDTO datos, Integer usuarioId, String idempotencyKey);

    /**
     * Registra una venta PRESENCIAL desde el mostrador (POS del
     * administrador), con la misma logica transaccional de stock. Nace
     * ya ENTREGADA porque se sirve al momento en el mostrador.
     */
    VentaResponseDTO registrarVentaPresencial(VentaPresencialRequestDTO datos, int usuarioAdminId);

    VentaResponseDTO obtenerPorToken(String token);

    VentaResponseDTO obtenerPorId(int id);

    List<VentaResponseDTO> listarHistorial(TipoVenta tipoVenta, int limite);

    List<VentaResponseDTO> listarFiltrado(VentaFiltroDTO filtro);

    /** Pedidos web del propio usuario autenticado, mas recientes primero. */
    List<VentaResponseDTO> listarPedidosDeUsuario(int usuarioId, int limite);

    /** Lanza AccesoDenegadoException si la venta no pertenece a ese usuario. */
    VentaResponseDTO obtenerPedidoDeUsuario(int usuarioId, int ventaId);

    /** Cambia el estado del PEDIDO validando la transicion; ver TransicionEstadoValidator. */
    VentaResponseDTO cambiarEstado(int ventaId, EstadoVenta nuevoEstado, int usuarioAdminId);

    /** Cambia el estado del PAGO, independiente del estado del pedido. */
    VentaResponseDTO cambiarEstadoPago(int ventaId, EstadoPago nuevoEstadoPago, int usuarioAdminId);

    /** Cancela la venta y devuelve el stock descontado, todo en una transaccion. */
    VentaResponseDTO cancelar(int ventaId, int usuarioAdminId);

    /**
     * Revisa las ventas EN_PREPARACION cuyo tiempo estimado ya se cumplio
     * y las mueve a LISTO. Devuelve cuantas se actualizaron. Pensado para
     * ser llamado periodicamente por el scheduler.
     */
    int procesarPreparacionesVencidas();
}
