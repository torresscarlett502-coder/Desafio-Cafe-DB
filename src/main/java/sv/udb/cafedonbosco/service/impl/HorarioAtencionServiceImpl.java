package sv.udb.cafedonbosco.service.impl;

import sv.udb.cafedonbosco.dao.HorarioAtencionDAO;
import sv.udb.cafedonbosco.dao.impl.HorarioAtencionDAOImpl;
import sv.udb.cafedonbosco.dto.request.HorarioAtencionRequestDTO;
import sv.udb.cafedonbosco.dto.response.EstadoLocalResponseDTO;
import sv.udb.cafedonbosco.dto.response.HorarioAtencionResponseDTO;
import sv.udb.cafedonbosco.exception.LocalCerradoException;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.HorarioAtencion;
import sv.udb.cafedonbosco.model.TipoVenta;
import sv.udb.cafedonbosco.service.HorarioAtencionService;
import sv.udb.cafedonbosco.util.FechaUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HorarioAtencionServiceImpl implements HorarioAtencionService {

    private static final DateTimeFormatter FORMATO_HORA_12H = DateTimeFormatter.ofPattern("hh:mm a", new Locale("es"));

    private final HorarioAtencionDAO horarioDAO;

    public HorarioAtencionServiceImpl() {
        this(new HorarioAtencionDAOImpl());
    }

    /** Permite inyectar un HorarioAtencionDAO de prueba (Mockito) sin tocar una base de datos real. */
    public HorarioAtencionServiceImpl(HorarioAtencionDAO horarioDAO) {
        this.horarioDAO = horarioDAO;
    }

    @Override
    public void validarLocalAbiertoParaPedido(TipoVenta tipoVenta) {
        LocalDateTime ahora = FechaUtil.obtenerFechaHoraActual();
        LocalTime horaActual = ahora.toLocalTime();
        HorarioAtencion horario = horarioDAO.obtenerPorDia(ahora.getDayOfWeek().getValue());

        if (horario == null) {
            throw new LocalCerradoException("No hay horario de atencion configurado para el dia de hoy.");
        }

        boolean dentroDeHorario = estaDentroDelHorario(horaActual, horario.getHoraApertura(), horario.getHoraCierre());
        boolean canalPermitido = tipoVenta == TipoVenta.WEB
                ? Boolean.TRUE.equals(horario.getPermitirPedidosApp())
                : Boolean.TRUE.equals(horario.getPermitirPedidosLocal());

        if (!dentroDeHorario || !canalPermitido) {
            // getHoraCierre().format(...) ya termina en "m." (a. m./p. m.),
            // asi que el mensaje no le agrega un punto final propio.
            throw new LocalCerradoException(String.format(
                    "El local se encuentra cerrado en este momento. Horario de atencion para hoy %s: de %s a %s",
                    horario.getNombreDia(),
                    horario.getHoraApertura().format(FORMATO_HORA_12H),
                    horario.getHoraCierre().format(FORMATO_HORA_12H)));
        }
    }

    @Override
    public EstadoLocalResponseDTO obtenerEstadoActual() {
        LocalDateTime ahora = FechaUtil.obtenerFechaHoraActual();
        HorarioAtencion horario = horarioDAO.obtenerPorDia(ahora.getDayOfWeek().getValue());
        LocalTime horaActual = ahora.toLocalTime();

        boolean abierto = horario != null
                && estaDentroDelHorario(horaActual, horario.getHoraApertura(), horario.getHoraCierre());

        return new EstadoLocalResponseDTO(
                abierto,
                ahora,
                horario != null ? horario.getNombreDia() : null,
                horario != null ? horario.getHoraApertura().format(FORMATO_HORA_12H) : "N/A",
                horario != null ? horario.getHoraCierre().format(FORMATO_HORA_12H) : "N/A",
                abierto ? "Estamos abiertos. Puedes hacer tu pedido."
                        : "Local cerrado. No se reciben pedidos en este momento."
        );
    }

    /**
     * Todos los horarios reales del negocio caen dentro del mismo dia
     * (ej. 06:30 a 20:30), pero el calculo no debe asumirlo: si algun dia
     * se configura un cierre despues de medianoche (cierre < apertura),
     * el rango cruza a la madrugada del dia siguiente y una comparacion
     * directa de LocalTime lo interpretaria al reves.
     */
    private boolean estaDentroDelHorario(LocalTime horaActual, LocalTime apertura, LocalTime cierre) {
        if (!cierre.isBefore(apertura)) {
            return !horaActual.isBefore(apertura) && !horaActual.isAfter(cierre);
        }
        return !horaActual.isBefore(apertura) || !horaActual.isAfter(cierre);
    }

    @Override
    public List<HorarioAtencionResponseDTO> listarTodos() {
        List<HorarioAtencionResponseDTO> resultado = new ArrayList<>();
        for (HorarioAtencion horario : horarioDAO.listarTodos()) {
            resultado.add(aResponseDTO(horario));
        }
        return resultado;
    }

    @Override
    public HorarioAtencionResponseDTO actualizar(int diaSemana, HorarioAtencionRequestDTO datos) {
        if (diaSemana < 1 || diaSemana > 7) {
            throw new ValidacionException("El dia de la semana debe estar entre 1 (lunes) y 7 (domingo).");
        }
        HorarioAtencion existente = horarioDAO.obtenerPorDia(diaSemana);
        if (existente == null) {
            throw new RecursoNoEncontradoException("No existe un horario configurado para ese dia.");
        }
        if (datos == null || datos.getHoraApertura() == null || datos.getHoraCierre() == null) {
            throw new ValidacionException("Debes indicar la hora de apertura y de cierre.");
        }

        LocalTime apertura = parsearHora(datos.getHoraApertura());
        LocalTime cierre = parsearHora(datos.getHoraCierre());
        if (!cierre.isAfter(apertura)) {
            throw new ValidacionException("La hora de cierre debe ser posterior a la hora de apertura.");
        }

        existente.setHoraApertura(apertura);
        existente.setHoraCierre(cierre);
        existente.setPermitirPedidosApp(datos.getPermitirPedidosApp() == null || datos.getPermitirPedidosApp());
        existente.setPermitirPedidosLocal(datos.getPermitirPedidosLocal() == null || datos.getPermitirPedidosLocal());
        horarioDAO.actualizar(existente);
        return aResponseDTO(existente);
    }

    private LocalTime parsearHora(String valor) {
        try {
            return LocalTime.parse(valor.trim().length() == 5 ? valor.trim() + ":00" : valor.trim());
        } catch (DateTimeParseException e) {
            throw new ValidacionException("La hora debe tener formato HH:mm o HH:mm:ss.");
        }
    }

    private HorarioAtencionResponseDTO aResponseDTO(HorarioAtencion horario) {
        HorarioAtencionResponseDTO dto = new HorarioAtencionResponseDTO();
        dto.setDiaSemana(horario.getDiaSemana());
        dto.setNombreDia(horario.getNombreDia());
        dto.setHoraApertura(horario.getHoraApertura().format(FORMATO_HORA_12H));
        dto.setHoraCierre(horario.getHoraCierre().format(FORMATO_HORA_12H));
        dto.setPermitirPedidosApp(horario.getPermitirPedidosApp());
        dto.setPermitirPedidosLocal(horario.getPermitirPedidosLocal());
        return dto;
    }
}
