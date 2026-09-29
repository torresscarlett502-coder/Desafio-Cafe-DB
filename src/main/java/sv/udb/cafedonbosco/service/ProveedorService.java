package sv.udb.cafedonbosco.service;

import sv.udb.cafedonbosco.dto.request.ProveedorRequestDTO;
import sv.udb.cafedonbosco.dto.response.ProveedorResponseDTO;

import java.util.List;

public interface ProveedorService {

    List<ProveedorResponseDTO> listarTodos();

    List<ProveedorResponseDTO> listarActivos();

    ProveedorResponseDTO obtenerPorId(int id);

    ProveedorResponseDTO crear(ProveedorRequestDTO datos);

    ProveedorResponseDTO actualizar(int id, ProveedorRequestDTO datos);
}
