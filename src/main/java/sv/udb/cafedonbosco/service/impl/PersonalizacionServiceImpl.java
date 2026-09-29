package sv.udb.cafedonbosco.service.impl;

import sv.udb.cafedonbosco.dao.GrupoOpcionDAO;
import sv.udb.cafedonbosco.dao.OpcionDAO;
import sv.udb.cafedonbosco.dao.ProductoDAO;
import sv.udb.cafedonbosco.dao.impl.GrupoOpcionDAOImpl;
import sv.udb.cafedonbosco.dao.impl.OpcionDAOImpl;
import sv.udb.cafedonbosco.dao.impl.ProductoDAOImpl;
import sv.udb.cafedonbosco.dto.request.GrupoOpcionRequestDTO;
import sv.udb.cafedonbosco.dto.request.OpcionRequestDTO;
import sv.udb.cafedonbosco.dto.response.GrupoOpcionResponseDTO;
import sv.udb.cafedonbosco.dto.response.OpcionResponseDTO;
import sv.udb.cafedonbosco.exception.RecursoDuplicadoException;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.GrupoOpcion;
import sv.udb.cafedonbosco.model.Opcion;
import sv.udb.cafedonbosco.model.OpcionSeleccionada;
import sv.udb.cafedonbosco.service.PersonalizacionService;
import sv.udb.cafedonbosco.util.ValidacionUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PersonalizacionServiceImpl implements PersonalizacionService {

    private final GrupoOpcionDAO grupoOpcionDAO;
    private final OpcionDAO opcionDAO;
    private final ProductoDAO productoDAO;

    public PersonalizacionServiceImpl() {
        this.grupoOpcionDAO = new GrupoOpcionDAOImpl();
        this.opcionDAO = new OpcionDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
    }

    public PersonalizacionServiceImpl(GrupoOpcionDAO grupoOpcionDAO, OpcionDAO opcionDAO, ProductoDAO productoDAO) {
        this.grupoOpcionDAO = grupoOpcionDAO;
        this.opcionDAO = opcionDAO;
        this.productoDAO = productoDAO;
    }

    @Override
    public List<GrupoOpcionResponseDTO> listarGrupos() {
        List<GrupoOpcionResponseDTO> resultado = new ArrayList<>();
        for (GrupoOpcion grupo : grupoOpcionDAO.listarTodos()) {
            resultado.add(aGrupoResponseDTO(grupo, opcionDAO.listarPorGrupo(grupo.getId())));
        }
        return resultado;
    }

    @Override
    public GrupoOpcionResponseDTO obtenerGrupoPorId(int id) {
        GrupoOpcion grupo = grupoOpcionDAO.buscarPorId(id);
        if (grupo == null) {
            throw new RecursoNoEncontradoException("El grupo de opciones solicitado no existe.");
        }
        return aGrupoResponseDTO(grupo, opcionDAO.listarPorGrupo(id));
    }

    @Override
    public GrupoOpcionResponseDTO crearGrupo(GrupoOpcionRequestDTO datos) {
        validarGrupo(datos);
        if (grupoOpcionDAO.existeNombre(datos.getNombre().trim(), null)) {
            throw new RecursoDuplicadoException("Ya existe un grupo de opciones llamado \"" + datos.getNombre().trim() + "\".");
        }
        GrupoOpcion grupo = new GrupoOpcion();
        aplicarDatosGrupo(grupo, datos);
        return aGrupoResponseDTO(grupoOpcionDAO.crear(grupo), new ArrayList<>());
    }

    @Override
    public GrupoOpcionResponseDTO actualizarGrupo(int id, GrupoOpcionRequestDTO datos) {
        validarGrupo(datos);
        GrupoOpcion existente = grupoOpcionDAO.buscarPorId(id);
        if (existente == null) {
            throw new RecursoNoEncontradoException("El grupo de opciones solicitado no existe.");
        }
        if (grupoOpcionDAO.existeNombre(datos.getNombre().trim(), id)) {
            throw new RecursoDuplicadoException("Ya existe otro grupo de opciones llamado \"" + datos.getNombre().trim() + "\".");
        }
        aplicarDatosGrupo(existente, datos);
        grupoOpcionDAO.actualizar(existente);
        return aGrupoResponseDTO(existente, opcionDAO.listarPorGrupo(existente.getId()));
    }

    @Override
    public OpcionResponseDTO crearOpcion(int grupoId, OpcionRequestDTO datos) {
        if (grupoOpcionDAO.buscarPorId(grupoId) == null) {
            throw new RecursoNoEncontradoException("El grupo de opciones solicitado no existe.");
        }
        validarOpcion(datos);
        if (opcionDAO.existeNombreEnGrupo(grupoId, datos.getNombre().trim(), null)) {
            throw new RecursoDuplicadoException("Ya existe una opcion llamada \"" + datos.getNombre().trim() + "\" en este grupo.");
        }
        Opcion opcion = new Opcion();
        opcion.setGrupoId(grupoId);
        aplicarDatosOpcion(opcion, datos);
        return aOpcionResponseDTO(opcionDAO.crear(opcion));
    }

    @Override
    public OpcionResponseDTO actualizarOpcion(int grupoId, int opcionId, OpcionRequestDTO datos) {
        if (grupoOpcionDAO.buscarPorId(grupoId) == null) {
            throw new RecursoNoEncontradoException("El grupo de opciones solicitado no existe.");
        }
        Opcion existente = opcionDAO.buscarPorId(opcionId);
        if (existente == null || !existente.getGrupoId().equals(grupoId)) {
            throw new RecursoNoEncontradoException("La opcion solicitada no existe en este grupo.");
        }
        validarOpcion(datos);
        if (opcionDAO.existeNombreEnGrupo(grupoId, datos.getNombre().trim(), opcionId)) {
            throw new RecursoDuplicadoException("Ya existe otra opcion llamada \"" + datos.getNombre().trim() + "\" en este grupo.");
        }
        aplicarDatosOpcion(existente, datos);
        opcionDAO.actualizar(existente);
        return aOpcionResponseDTO(existente);
    }

    @Override
    public List<GrupoOpcionResponseDTO> listarGruposDeProducto(int productoId) {
        if (productoDAO.buscarPorId(productoId) == null) {
            throw new RecursoNoEncontradoException("El producto solicitado no existe.");
        }
        List<GrupoOpcionResponseDTO> resultado = new ArrayList<>();
        for (GrupoOpcion grupo : grupoOpcionDAO.listarPorProducto(productoId)) {
            resultado.add(aGrupoResponseDTO(grupo, opcionDAO.listarActivasPorGrupo(grupo.getId())));
        }
        return resultado;
    }

    @Override
    public void asociarGrupoAProducto(int productoId, int grupoId) {
        if (productoDAO.buscarPorId(productoId) == null) {
            throw new RecursoNoEncontradoException("El producto solicitado no existe.");
        }
        if (grupoOpcionDAO.buscarPorId(grupoId) == null) {
            throw new RecursoNoEncontradoException("El grupo de opciones solicitado no existe.");
        }
        grupoOpcionDAO.asociarAProducto(productoId, grupoId);
    }

    @Override
    public void desasociarGrupoDeProducto(int productoId, int grupoId) {
        grupoOpcionDAO.desasociarDeProducto(productoId, grupoId);
    }

    @Override
    public List<OpcionSeleccionada> validarYResolverOpciones(int productoId, List<Integer> opcionIds) {
        if (productoDAO.buscarPorId(productoId) == null) {
            throw new RecursoNoEncontradoException("El producto solicitado no existe.");
        }

        List<GrupoOpcion> grupos = grupoOpcionDAO.listarPorProducto(productoId);

        Set<Integer> idsSolicitados = new LinkedHashSet<>();
        if (opcionIds != null) {
            for (Integer id : opcionIds) {
                if (id != null) {
                    idsSolicitados.add(id);
                }
            }
        }

        Map<Integer, Opcion> opcionesValidasPorId = new LinkedHashMap<>();
        Map<Integer, GrupoOpcion> grupoDeOpcion = new LinkedHashMap<>();
        for (GrupoOpcion grupo : grupos) {
            for (Opcion opcion : opcionDAO.listarActivasPorGrupo(grupo.getId())) {
                opcionesValidasPorId.put(opcion.getId(), opcion);
                grupoDeOpcion.put(opcion.getId(), grupo);
            }
        }

        List<OpcionSeleccionada> seleccionadas = new ArrayList<>();
        Map<Integer, Integer> conteoPorGrupo = new HashMap<>();
        for (Integer idSolicitado : idsSolicitados) {
            Opcion opcion = opcionesValidasPorId.get(idSolicitado);
            if (opcion == null) {
                throw new ValidacionException("La opcion seleccionada no esta disponible para este producto.");
            }
            GrupoOpcion grupo = grupoDeOpcion.get(idSolicitado);
            int conteoActual = conteoPorGrupo.getOrDefault(grupo.getId(), 0) + 1;
            conteoPorGrupo.put(grupo.getId(), conteoActual);
            if (!Boolean.TRUE.equals(grupo.getSeleccionMultiple()) && conteoActual > 1) {
                throw new ValidacionException("Solo puedes elegir una opcion del grupo \"" + grupo.getNombre() + "\".");
            }
            seleccionadas.add(new OpcionSeleccionada(opcion.getId(), grupo.getNombre(), opcion.getNombre(), opcion.getPrecioAdicional()));
        }

        for (GrupoOpcion grupo : grupos) {
            if (Boolean.TRUE.equals(grupo.getObligatorio()) && conteoPorGrupo.getOrDefault(grupo.getId(), 0) == 0) {
                throw new ValidacionException("Debes elegir una opcion del grupo \"" + grupo.getNombre() + "\".");
            }
        }

        return seleccionadas;
    }

    private void validarGrupo(GrupoOpcionRequestDTO datos) {
        if (datos == null || !ValidacionUtil.esTextoValido(datos.getNombre(), 50)) {
            throw new ValidacionException("El nombre del grupo de opciones es obligatorio.");
        }
    }

    private void validarOpcion(OpcionRequestDTO datos) {
        if (datos == null || !ValidacionUtil.esTextoValido(datos.getNombre(), 50)) {
            throw new ValidacionException("El nombre de la opcion es obligatorio.");
        }
        if (datos.getPrecioAdicional() != null && datos.getPrecioAdicional().signum() < 0) {
            throw new ValidacionException("El precio adicional de la opcion no puede ser negativo.");
        }
    }

    private void aplicarDatosGrupo(GrupoOpcion grupo, GrupoOpcionRequestDTO datos) {
        grupo.setNombre(datos.getNombre().trim());
        grupo.setObligatorio(Boolean.TRUE.equals(datos.getObligatorio()));
        grupo.setSeleccionMultiple(Boolean.TRUE.equals(datos.getSeleccionMultiple()));
        grupo.setActivo(datos.getActivo() == null || datos.getActivo());
    }

    private void aplicarDatosOpcion(Opcion opcion, OpcionRequestDTO datos) {
        opcion.setNombre(datos.getNombre().trim());
        opcion.setPrecioAdicional(datos.getPrecioAdicional() != null ? datos.getPrecioAdicional() : BigDecimal.ZERO);
        opcion.setActivo(datos.getActivo() == null || datos.getActivo());
    }

    private GrupoOpcionResponseDTO aGrupoResponseDTO(GrupoOpcion grupo, List<Opcion> opciones) {
        GrupoOpcionResponseDTO dto = new GrupoOpcionResponseDTO();
        dto.setId(grupo.getId());
        dto.setNombre(grupo.getNombre());
        dto.setObligatorio(grupo.getObligatorio());
        dto.setSeleccionMultiple(grupo.getSeleccionMultiple());
        dto.setActivo(grupo.getActivo());
        List<OpcionResponseDTO> opcionesDTO = new ArrayList<>();
        for (Opcion opcion : opciones) {
            opcionesDTO.add(aOpcionResponseDTO(opcion));
        }
        dto.setOpciones(opcionesDTO);
        return dto;
    }

    private OpcionResponseDTO aOpcionResponseDTO(Opcion opcion) {
        OpcionResponseDTO dto = new OpcionResponseDTO();
        dto.setId(opcion.getId());
        dto.setGrupoId(opcion.getGrupoId());
        dto.setNombre(opcion.getNombre());
        dto.setPrecioAdicional(opcion.getPrecioAdicional());
        dto.setActivo(opcion.getActivo());
        return dto;
    }
}
