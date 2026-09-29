package sv.udb.cafedonbosco.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.request.CompraRequestDTO;
import sv.udb.cafedonbosco.dto.response.CompraResponseDTO;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.exception.AccesoDenegadoException;
import sv.udb.cafedonbosco.service.CompraService;
import sv.udb.cafedonbosco.service.impl.CompraServiceImpl;
import sv.udb.cafedonbosco.util.JsonUtil;
import sv.udb.cafedonbosco.util.SessionUtil;

import java.io.IOException;

@WebServlet(name = "CompraServlet", urlPatterns = "/api/admin/compras")
public class CompraServlet extends BaseServlet {

    private final CompraService compraService = new CompraServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            JsonUtil.exito(response, HttpServletResponse.SC_OK, "Compras obtenidas", compraService.listarTodas());
        } catch (Exception e) {
            manejarError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UsuarioResponseDTO administrador = SessionUtil.obtenerUsuarioAutenticado(request);
            if (administrador == null) {
                throw new AccesoDenegadoException("Debes iniciar sesion como administrador.");
            }
            CompraRequestDTO datos = JsonUtil.leerCuerpo(request, CompraRequestDTO.class);
            CompraResponseDTO compra = compraService.registrar(datos, administrador.getId());
            JsonUtil.exito(response, HttpServletResponse.SC_CREATED, "Compra registrada correctamente", compra);
        } catch (Exception e) {
            manejarError(response, e);
        }
    }
}
