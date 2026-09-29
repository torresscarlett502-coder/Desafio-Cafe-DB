package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.GrupoOpcionRequestDTO;
import sv.udb.cafedonbosco.dto.request.OpcionRequestDTO;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.service.PersonalizacionService;
import sv.udb.cafedonbosco.service.impl.PersonalizacionServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;

import java.io.IOException;

/**
 * Gestion administrativa de la personalizacion de productos (grupos de
 * opciones como "Tipo de leche" y sus opciones), siempre bajo /api/admin
 * (protegido por RolAdminFilter):
 *   GET    /api/admin/grupos-opcion                          listar todos
 *   GET    /api/admin/grupos-opcion/{id}                     detalle
 *   POST   /api/admin/grupos-opcion                          crear grupo
 *   PUT    /api/admin/grupos-opcion/{id}                     actualizar grupo
 *   POST   /api/admin/grupos-opcion/{id}/opciones             crear opcion en el grupo
 *   PUT    /api/admin/grupos-opcion/{id}/opciones/{opcionId} actualizar opcion
 *   POST   /api/admin/grupos-opcion/{id}/productos/{prodId}  asociar grupo al producto
 *   DELETE /api/admin/grupos-opcion/{id}/productos/{prodId}  desasociar grupo del producto
 */
@WebServlet(name = "GrupoOpcionServlet", urlPatterns = {"/api/admin/grupos-opcion", "/api/admin/grupos-opcion/*"})
public class GrupoOpcionServlet extends BaseServlet {

    private final PersonalizacionService personalizacionService = new PersonalizacionServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length == 0) {
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Grupos de opciones obtenidos",
                        personalizacionService.listarGrupos());
                return;
            }
            int id = Integer.parseInt(segmentos[0]);
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Grupo de opciones obtenido",
                    personalizacionService.obtenerGrupoPorId(id));
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length == 0) {
                GrupoOpcionRequestDTO datos = JsonUtil.leerCuerpo(request, GrupoOpcionRequestDTO.class);
                JsonUtil.exito(response, HttpServletResponse.SC_CREATED, "Grupo de opciones creado correctamente",
                        personalizacionService.crearGrupo(datos));
                return;
            }
            int grupoId = Integer.parseInt(segmentos[0]);

            if (segmentos.length == 2 && "opciones".equals(segmentos[1])) {
                OpcionRequestDTO datos = JsonUtil.leerCuerpo(request, OpcionRequestDTO.class);
                JsonUtil.exito(response, HttpServletResponse.SC_CREATED, "Opcion creada correctamente",
                        personalizacionService.crearOpcion(grupoId, datos));
                return;
            }

            if (segmentos.length == 3 && "productos".equals(segmentos[1])) {
                int productoId = Integer.parseInt(segmentos[2]);
                personalizacionService.asociarGrupoAProducto(productoId, grupoId);
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Grupo asociado al producto", null);
                return;
            }

            throw new ValidacionException("Ruta no valida.");
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length == 0) {
                throw new ValidacionException("Debes indicar el id del grupo en la URL.");
            }
            int grupoId = Integer.parseInt(segmentos[0]);

            if (segmentos.length == 1) {
                GrupoOpcionRequestDTO datos = JsonUtil.leerCuerpo(request, GrupoOpcionRequestDTO.class);
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Grupo de opciones actualizado correctamente",
                        personalizacionService.actualizarGrupo(grupoId, datos));
                return;
            }

            if (segmentos.length == 3 && "opciones".equals(segmentos[1])) {
                int opcionId = Integer.parseInt(segmentos[2]);
                OpcionRequestDTO datos = JsonUtil.leerCuerpo(request, OpcionRequestDTO.class);
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Opcion actualizada correctamente",
                        personalizacionService.actualizarOpcion(grupoId, opcionId, datos));
                return;
            }

            throw new ValidacionException("Ruta no valida.");
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String[] segmentos = segmentosDePath(request);
            if (segmentos.length == 3 && "productos".equals(segmentos[1])) {
                int grupoId = Integer.parseInt(segmentos[0]);
                int productoId = Integer.parseInt(segmentos[2]);
                personalizacionService.desasociarGrupoDeProducto(productoId, grupoId);
                JsonUtil.exito(response, HttpServletResponse.SC_OK, "Grupo desasociado del producto", null);
                return;
            }
            throw new ValidacionException("Ruta no valida.");
        } catch (NumberFormatException e) {
            JsonUtil.error(response, HttpServletResponse.SC_BAD_REQUEST, "El id no es valido.");
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    private String[] segmentosDePath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return new String[0];
        }
        String limpio = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        return limpio.split("/");
    }
}
