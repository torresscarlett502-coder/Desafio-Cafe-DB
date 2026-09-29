package sv.udb.cafedonbosco.service;

import sv.udb.cafedonbosco.dto.request.HorarioAtencionRequestDTO;
import sv.udb.cafedonbosco.dto.response.EstadoLocalResponseDTO;
import sv.udb.cafedonbosco.dto.response.HorarioAtencionResponseDTO;
import sv.udb.cafedonbosco.model.TipoVenta;

import java.util.List;

public interface HorarioAtencionService {

    /** Lanza LocalCerradoException si el local esta cerrado ahora mismo para ese canal. */
    void validarLocalAbiertoParaPedido(TipoVenta tipoVenta);

    EstadoLocalResponseDTO obtenerEstadoActual();

    List<HorarioAtencionResponseDTO> listarTodos();

    HorarioAtencionResponseDTO actualizar(int diaSemana, HorarioAtencionRequestDTO datos);
}
