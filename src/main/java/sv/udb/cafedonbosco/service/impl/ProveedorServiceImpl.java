package sv.udb.cafedonbosco.service.impl;

import sv.udb.cafedonbosco.dao.ProveedorDAO;
import sv.udb.cafedonbosco.dao.impl.ProveedorDAOImpl;
import sv.udb.cafedonbosco.dto.request.ProveedorRequestDTO;
import sv.udb.cafedonbosco.dto.response.ProveedorResponseDTO;
import sv.udb.cafedonbosco.exception.RecursoDuplicadoException;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.Proveedor;
import sv.udb.cafedonbosco.service.ProveedorService;
import sv.udb.cafedonbosco.util.ValidacionUtil;

import java.util.ArrayList;
import java.util.List;

public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorDAO proveedorDAO;

    public ProveedorServiceImpl() {
        this.proveedorDAO = new ProveedorDAOImpl();
    }

    @Override
    public List<ProveedorResponseDTO> listarTodos() {
        List<ProveedorResponseDTO> resultado = new ArrayList<>();
        for (Proveedor proveedor : proveedorDAO.listarTodos()) {
            resultado.add(aResponseDTO(proveedor));
        }
        return resultado;
    }

    @Override
    public List<ProveedorResponseDTO> listarActivos() {
        List<ProveedorResponseDTO> resultado = new ArrayList<>();
        for (Proveedor proveedor : proveedorDAO.listarActivos()) {
            resultado.add(aResponseDTO(proveedor));
        }
        return resultado;
    }

    @Override
    public ProveedorResponseDTO obtenerPorId(int id) {
        Proveedor proveedor = proveedorDAO.buscarPorId(id);
        if (proveedor == null) {
            throw new RecursoNoEncontradoException("El proveedor solicitado no existe.");
        }
        return aResponseDTO(proveedor);
    }

    @Override
    public ProveedorResponseDTO crear(ProveedorRequestDTO datos) {
        validar(datos);
        if (proveedorDAO.existeNombre(datos.getNombre().trim(), null)) {
            throw new RecursoDuplicadoException("Ya existe un proveedor llamado \"" + datos.getNombre().trim() + "\".");
        }

        Proveedor proveedor = new Proveedor();
        aplicarDatos(proveedor, datos);
        return aResponseDTO(proveedorDAO.crear(proveedor));
    }

    @Override
    public ProveedorResponseDTO actualizar(int id, ProveedorRequestDTO datos) {
        validar(datos);
        Proveedor existente = proveedorDAO.buscarPorId(id);
        if (existente == null) {
            throw new RecursoNoEncontradoException("El proveedor solicitado no existe.");
        }
        if (proveedorDAO.existeNombre(datos.getNombre().trim(), id)) {
            throw new RecursoDuplicadoException("Ya existe otro proveedor llamado \"" + datos.getNombre().trim() + "\".");
        }

        aplicarDatos(existente, datos);
        proveedorDAO.actualizar(existente);
        return aResponseDTO(existente);
    }

    private void validar(ProveedorRequestDTO datos) {
        if (datos == null || !ValidacionUtil.esTextoValido(datos.getNombre(), 150)) {
            throw new ValidacionException("El nombre del proveedor es obligatorio.");
        }
        if (datos.getCorreo() != null && !datos.getCorreo().isBlank() && !ValidacionUtil.esCorreoValido(datos.getCorreo())) {
            throw new ValidacionException("El correo del proveedor no es valido.");
        }
        if (datos.getTelefono() != null && !datos.getTelefono().isBlank() && !ValidacionUtil.esTelefonoValido(datos.getTelefono())) {
            throw new ValidacionException("El telefono del proveedor no es valido.");
        }
    }

    private void aplicarDatos(Proveedor proveedor, ProveedorRequestDTO datos) {
        proveedor.setNombre(datos.getNombre().trim());
        proveedor.setContacto(datos.getContacto());
        proveedor.setTelefono(datos.getTelefono());
        proveedor.setCorreo(datos.getCorreo());
        proveedor.setDireccion(datos.getDireccion());
        proveedor.setActivo(datos.getActivo() == null || datos.getActivo());
    }

    private ProveedorResponseDTO aResponseDTO(Proveedor proveedor) {
        ProveedorResponseDTO dto = new ProveedorResponseDTO();
        dto.setId(proveedor.getId());
        dto.setNombre(proveedor.getNombre());
        dto.setContacto(proveedor.getContacto());
        dto.setTelefono(proveedor.getTelefono());
        dto.setCorreo(proveedor.getCorreo());
        dto.setDireccion(proveedor.getDireccion());
        dto.setActivo(proveedor.getActivo());
        return dto;
    }
}
