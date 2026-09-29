package sv.udb.cafedonbosco.service;

import sv.udb.cafedonbosco.dto.request.CompraRequestDTO;
import sv.udb.cafedonbosco.dto.response.CompraResponseDTO;

import java.util.List;

public interface CompraService {

    CompraResponseDTO registrar(CompraRequestDTO datos, int usuarioAdminId);

    List<CompraResponseDTO> listarTodas();
}
