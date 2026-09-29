package sv.udb.cafedonbosco.service;

import sv.udb.cafedonbosco.dto.request.InventarioAjusteRequestDTO;
import sv.udb.cafedonbosco.dto.response.MovimientoInventarioResponseDTO;
import sv.udb.cafedonbosco.dto.response.ProductoAdminResponseDTO;

import java.util.List;

public interface InventarioService {

    List<ProductoAdminResponseDTO> listarInventario();

    /** Ajuste manual de stock/stock minimo; queda registrado en el movimiento de inventario y en la bitacora. */
    void ajustar(InventarioAjusteRequestDTO datos, int usuarioAdminId);

    /** Historial de movimientos de inventario, opcionalmente filtrado por producto. */
    List<MovimientoInventarioResponseDTO> listarMovimientos(Integer productoId, int limite);
}
