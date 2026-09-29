package sv.udb.cafedonbosco.dao;

import sv.udb.cafedonbosco.dto.request.VentaFiltroDTO;
import sv.udb.cafedonbosco.model.DetalleVenta;
import sv.udb.cafedonbosco.model.EstadoPago;
import sv.udb.cafedonbosco.model.EstadoVenta;
import sv.udb.cafedonbosco.model.TipoVenta;
import sv.udb.cafedonbosco.model.Venta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public interface VentaDAO {

    /**
     * Inserta la cabecera de la venta dentro de una transaccion ya abierta
     * y devuelve el id generado en venta.setId(...).
     */
    Venta crear(Connection conexion, Venta venta);

    void crearDetalle(Connection conexion, DetalleVenta detalle, int ventaId);

    Venta buscarPorId(int id);

    Venta buscarPorToken(String token);

    /**
     * Igual que buscarPorId pero con SELECT ... FOR UPDATE dentro de la
     * transaccion en curso: bloquea la fila hasta el commit/rollback para
     * que dos solicitudes de cambio de estado simultaneas sobre la misma
     * venta (ej. dos clics de "cancelar") no se pisen entre si.
     */
    Venta buscarPorIdParaActualizar(Connection conexion, int id);

    boolean existeIdempotencyKey(String idempotencyKey);

    Venta buscarPorIdempotencyKey(String idempotencyKey);

    List<Venta> listarHistorial(TipoVenta tipoVenta, int limite);

    List<Venta> listarRecientes(int limite);

    List<Venta> listarFiltrado(VentaFiltroDTO filtro);

    List<Venta> listarPorUsuario(int usuarioId, int limite);

    /** Ventas EN_PREPARACION cuyo tiempo estimado ya se cumplio (para el scheduler). */
    List<Venta> listarEnPreparacionVencidas();

    void iniciarPreparacion(Connection conexion, int ventaId, LocalDateTime inicio, LocalDateTime estimadaListo);

    void marcarListo(Connection conexion, int ventaId, LocalDateTime listoEn);

    void marcarEntregado(Connection conexion, int ventaId, LocalDateTime entregadoEn);

    void marcarCancelado(Connection conexion, int ventaId);

    void actualizarEstadoPago(Connection conexion, int ventaId, EstadoPago nuevoEstadoPago);

    BigDecimal sumarTotalDelDia();

    int contarVentasDelDia();

    BigDecimal sumarTotalDelMes();

    /** Suma solo lo realmente cobrado: estado_pago = APROBADO y no cancelada. */
    BigDecimal sumarIngresosCobrados();

    int contarPorEstado(EstadoVenta estado);
}
