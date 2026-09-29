package sv.udb.cafedonbosco.service;

import sv.udb.cafedonbosco.dto.request.CategoriaRequestDTO;
import sv.udb.cafedonbosco.dto.response.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {

    List<CategoriaResponseDTO> listarActivas();

    List<CategoriaResponseDTO> listarTodas();

    CategoriaResponseDTO crear(CategoriaRequestDTO datos);

    CategoriaResponseDTO actualizar(int id, CategoriaRequestDTO datos);
}
