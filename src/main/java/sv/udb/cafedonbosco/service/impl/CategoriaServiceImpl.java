package sv.udb.cafedonbosco.service.impl;

import sv.udb.cafedonbosco.dao.CategoriaDAO;
import sv.udb.cafedonbosco.dao.impl.CategoriaDAOImpl;
import sv.udb.cafedonbosco.dto.request.CategoriaRequestDTO;
import sv.udb.cafedonbosco.dto.response.CategoriaResponseDTO;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.Categoria;
import sv.udb.cafedonbosco.service.CategoriaService;
import sv.udb.cafedonbosco.util.ValidacionUtil;

import java.util.ArrayList;
import java.util.List;

public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaDAO categoriaDAO;

    public CategoriaServiceImpl() {
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    @Override
    public List<CategoriaResponseDTO> listarActivas() {
        return mapear(categoriaDAO.listarActivas());
    }

    @Override
    public List<CategoriaResponseDTO> listarTodas() {
        return mapear(categoriaDAO.listarTodas());
    }

    private List<CategoriaResponseDTO> mapear(List<Categoria> categorias) {
        List<CategoriaResponseDTO> resultado = new ArrayList<>();
        for (Categoria categoria : categorias) {
            int cantidad = categoriaDAO.contarProductosActivos(categoria.getId());
            resultado.add(new CategoriaResponseDTO(
                    categoria.getId(), categoria.getNombre(), categoria.getDescripcion(),
                    categoria.getActivo(), cantidad
            ));
        }
        return resultado;
    }

    @Override
    public CategoriaResponseDTO crear(CategoriaRequestDTO datos) {
        validar(datos);
        Categoria categoria = new Categoria();
        categoria.setNombre(datos.getNombre().trim());
        categoria.setDescripcion(datos.getDescripcion());
        categoria.setActivo(datos.getActivo() == null || datos.getActivo());
        Categoria creada = categoriaDAO.crear(categoria);
        return new CategoriaResponseDTO(creada.getId(), creada.getNombre(), creada.getDescripcion(), creada.getActivo(), 0);
    }

    @Override
    public CategoriaResponseDTO actualizar(int id, CategoriaRequestDTO datos) {
        validar(datos);
        Categoria existente = categoriaDAO.buscarPorId(id);
        if (existente == null) {
            throw new RecursoNoEncontradoException("La categoria solicitada no existe.");
        }
        existente.setNombre(datos.getNombre().trim());
        existente.setDescripcion(datos.getDescripcion());
        existente.setActivo(datos.getActivo() == null ? existente.getActivo() : datos.getActivo());
        categoriaDAO.actualizar(existente);
        int cantidad = categoriaDAO.contarProductosActivos(id);
        return new CategoriaResponseDTO(existente.getId(), existente.getNombre(), existente.getDescripcion(), existente.getActivo(), cantidad);
    }

    private void validar(CategoriaRequestDTO datos) {
        if (datos == null || !ValidacionUtil.esTextoValido(datos.getNombre(), 60)) {
            throw new ValidacionException("El nombre de la categoria es obligatorio.");
        }
    }
}
