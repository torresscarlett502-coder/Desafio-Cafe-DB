package sv.udb.cafedonbosco.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.udb.cafedonbosco.dao.HorarioAtencionDAO;
import sv.udb.cafedonbosco.dto.request.HorarioAtencionRequestDTO;
import sv.udb.cafedonbosco.dto.response.EstadoLocalResponseDTO;
import sv.udb.cafedonbosco.exception.LocalCerradoException;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.HorarioAtencion;
import sv.udb.cafedonbosco.model.TipoVenta;
import sv.udb.cafedonbosco.service.HorarioAtencionService;
import sv.udb.cafedonbosco.util.FechaUtil;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Las pruebas usan horas relativas a FechaUtil.obtenerHoraActual() (nunca
 * horas fijas como "06:00") para no depender de a que hora del dia
 * corran realmente: "dentro de horario" siempre se arma como
 * [ahora-1h, ahora+1h] y "fuera de horario" como [ahora+1h, ahora+2h].
 */
@ExtendWith(MockitoExtension.class)
class HorarioAtencionServiceImplTest {

    @Mock
    private HorarioAtencionDAO horarioDAO;

    private HorarioAtencionService horarioService;

    @BeforeEach
    void configurar() {
        horarioService = new HorarioAtencionServiceImpl(horarioDAO);
    }

    private HorarioAtencion horarioAbiertoAhora() {
        LocalTime ahora = FechaUtil.obtenerHoraActual();
        HorarioAtencion horario = new HorarioAtencion();
        horario.setDiaSemana(1);
        horario.setNombreDia("LUNES");
        horario.setHoraApertura(ahora.minusHours(1));
        horario.setHoraCierre(ahora.plusHours(1));
        horario.setPermitirPedidosApp(true);
        horario.setPermitirPedidosLocal(true);
        return horario;
    }

    private HorarioAtencion horarioCerradoAhora() {
        LocalTime ahora = FechaUtil.obtenerHoraActual();
        HorarioAtencion horario = new HorarioAtencion();
        horario.setDiaSemana(1);
        horario.setNombreDia("LUNES");
        horario.setHoraApertura(ahora.plusHours(1));
        horario.setHoraCierre(ahora.plusHours(2));
        horario.setPermitirPedidosApp(true);
        horario.setPermitirPedidosLocal(true);
        return horario;
    }

    @Test
    void noLanzaNadaSiElLocalEstaAbiertoYElCanalHabilitado() {
        when(horarioDAO.obtenerPorDia(anyInt())).thenReturn(horarioAbiertoAhora());

        horarioService.validarLocalAbiertoParaPedido(TipoVenta.WEB);
        horarioService.validarLocalAbiertoParaPedido(TipoVenta.PRESENCIAL);
    }

    @Test
    void rechazaElPedidoSiLaHoraActualEstaFueraDelRangoConfigurado() {
        when(horarioDAO.obtenerPorDia(anyInt())).thenReturn(horarioCerradoAhora());

        assertThrows(LocalCerradoException.class, () -> horarioService.validarLocalAbiertoParaPedido(TipoVenta.WEB));
    }

    @Test
    void rechazaUnPedidoWebSiElCanalAppEstaDeshabilitadoAunqueEsteEnHorario() {
        HorarioAtencion horario = horarioAbiertoAhora();
        horario.setPermitirPedidosApp(false);
        when(horarioDAO.obtenerPorDia(anyInt())).thenReturn(horario);

        assertThrows(LocalCerradoException.class, () -> horarioService.validarLocalAbiertoParaPedido(TipoVenta.WEB));
    }

    @Test
    void unCanalDeshabilitadoNoAfectaAlOtroCanal() {
        // Si solo se deshabilita el canal APP, una venta PRESENCIAL en el
        // mismo horario debe seguir aceptandose.
        HorarioAtencion horario = horarioAbiertoAhora();
        horario.setPermitirPedidosApp(false);
        when(horarioDAO.obtenerPorDia(anyInt())).thenReturn(horario);

        horarioService.validarLocalAbiertoParaPedido(TipoVenta.PRESENCIAL);
    }

    @Test
    void rechazaUnaVentaPresencialSiElCanalLocalEstaDeshabilitado() {
        HorarioAtencion horario = horarioAbiertoAhora();
        horario.setPermitirPedidosLocal(false);
        when(horarioDAO.obtenerPorDia(anyInt())).thenReturn(horario);

        assertThrows(LocalCerradoException.class, () -> horarioService.validarLocalAbiertoParaPedido(TipoVenta.PRESENCIAL));
    }

    @Test
    void siNoHayHorarioConfiguradoParaElDiaSeRechazaElPedido() {
        when(horarioDAO.obtenerPorDia(anyInt())).thenReturn(null);

        assertThrows(LocalCerradoException.class, () -> horarioService.validarLocalAbiertoParaPedido(TipoVenta.WEB));
    }

    @Test
    void elMensajeDeErrorIndicaElHorarioRealDelDia() {
        when(horarioDAO.obtenerPorDia(anyInt())).thenReturn(horarioCerradoAhora());

        LocalCerradoException excepcion = assertThrows(LocalCerradoException.class,
                () -> horarioService.validarLocalAbiertoParaPedido(TipoVenta.WEB));

        assertTrue(excepcion.getMessage().contains("LUNES"));
    }

    @Test
    void obtenerEstadoActualReportaAbiertoDentroDeHorario() {
        when(horarioDAO.obtenerPorDia(anyInt())).thenReturn(horarioAbiertoAhora());

        EstadoLocalResponseDTO estado = horarioService.obtenerEstadoActual();

        assertTrue(estado.isAbierto());
    }

    @Test
    void obtenerEstadoActualReportaCerradoFueraDeHorario() {
        when(horarioDAO.obtenerPorDia(anyInt())).thenReturn(horarioCerradoAhora());

        EstadoLocalResponseDTO estado = horarioService.obtenerEstadoActual();

        assertFalse(estado.isAbierto());
    }

    @Test
    void actualizarConDiaFueraDeRangoSeRechaza() {
        assertThrows(ValidacionException.class, () -> horarioService.actualizar(0,
                datosHorario("08:00", "18:00")));
        assertThrows(ValidacionException.class, () -> horarioService.actualizar(8,
                datosHorario("08:00", "18:00")));
    }

    @Test
    void actualizarUnDiaSinConfiguracionPreviaSeRechaza() {
        when(horarioDAO.obtenerPorDia(3)).thenReturn(null);

        assertThrows(RecursoNoEncontradoException.class,
                () -> horarioService.actualizar(3, datosHorario("08:00", "18:00")));
    }

    @Test
    void actualizarConHoraDeCierreAntesQueLaDeAperturaSeRechaza() {
        when(horarioDAO.obtenerPorDia(1)).thenReturn(horarioAbiertoAhora());

        assertThrows(ValidacionException.class,
                () -> horarioService.actualizar(1, datosHorario("18:00", "08:00")));
    }

    @Test
    void actualizarConDatosValidosGuardaElNuevoHorario() {
        when(horarioDAO.obtenerPorDia(1)).thenReturn(horarioAbiertoAhora());

        horarioService.actualizar(1, datosHorario("07:00", "19:00"));

        ArgumentCaptor<HorarioAtencion> capturado = ArgumentCaptor.forClass(HorarioAtencion.class);
        verify(horarioDAO).actualizar(capturado.capture());
        assertEquals(LocalTime.of(7, 0), capturado.getValue().getHoraApertura());
        assertEquals(LocalTime.of(19, 0), capturado.getValue().getHoraCierre());
    }

    private HorarioAtencionRequestDTO datosHorario(String apertura, String cierre) {
        HorarioAtencionRequestDTO datos = new HorarioAtencionRequestDTO();
        datos.setHoraApertura(apertura);
        datos.setHoraCierre(cierre);
        datos.setPermitirPedidosApp(true);
        datos.setPermitirPedidosLocal(true);
        return datos;
    }
}
