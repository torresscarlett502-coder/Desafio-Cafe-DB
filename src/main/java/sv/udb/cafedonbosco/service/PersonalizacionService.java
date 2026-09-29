package sv.udb.cafedonbosco.service;

import sv.udb.cafedonbosco.dto.request.GrupoOpcionRequestDTO;
import sv.udb.cafedonbosco.dto.request.OpcionRequestDTO;
import sv.udb.cafedonbosco.dto.response.GrupoOpcionResponseDTO;
import sv.udb.cafedonbosco.dto.response.OpcionResponseDTO;
import sv.udb.cafedonbosco.model.OpcionSeleccionada;

import java.util.List;

public interface PersonalizacionService {

    List<GrupoOpcionResponseDTO> listarGrupos();

    GrupoOpcionResponseDTO obtenerGrupoPorId(int id);

    GrupoOpcionResponseDTO crearGrupo(GrupoOpcionRequestDTO datos);

    GrupoOpcionResponseDTO actualizarGrupo(int id, GrupoOpcionRequestDTO datos);

    OpcionResponseDTO crearOpcion(int grupoId, OpcionRequestDTO datos);

    OpcionResponseDTO actualizarOpcion(int grupoId, int opcionId, OpcionRequestDTO datos);

    /** Grupos activos (con sus opciones activas) asociados a un producto; para el catalogo publico. */
    List<GrupoOpcionResponseDTO> listarGruposDeProducto(int productoId);

    void asociarGrupoAProducto(int productoId, int grupoId);

    void desasociarGrupoDeProducto(int productoId, int grupoId);

    /**
     * Valida las opciones elegidas para un producto contra sus grupos
     * asociados (pertenencia, estado activo, cardinalidad segun
     * obligatorio/seleccionMultiple) y devuelve la fotografia de cada
     * opcion valida lista para guardarse en el carrito o la venta.
     */
    List<OpcionSeleccionada> validarYResolverOpciones(int productoId, List<Integer> opcionIds);
}
