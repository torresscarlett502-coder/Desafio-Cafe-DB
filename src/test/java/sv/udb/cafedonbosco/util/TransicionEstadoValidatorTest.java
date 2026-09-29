package sv.udb.cafedonbosco.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import sv.udb.cafedonbosco.exception.TransicionInvalidaException;
import sv.udb.cafedonbosco.model.EstadoVenta;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TransicionEstadoValidator es la unica fuente de verdad de que cambios
 * de estado de un pedido son validos; un error aqui abriria o cerraria
 * transiciones para todo el sistema (checkout, panel admin, scheduler).
 */
class TransicionEstadoValidatorTest {

    @Test
    void permiteElCicloNormalDeUnPedido() {
        assertTrue(TransicionEstadoValidator.esValida(EstadoVenta.RECIBIDO, EstadoVenta.EN_PREPARACION));
        assertTrue(TransicionEstadoValidator.esValida(EstadoVenta.EN_PREPARACION, EstadoVenta.LISTO));
        assertTrue(TransicionEstadoValidator.esValida(EstadoVenta.LISTO, EstadoVenta.ENTREGADO));
    }

    @Test
    void permiteCancelarDesdeCualquierEstadoNoTerminal() {
        assertTrue(TransicionEstadoValidator.esValida(EstadoVenta.RECIBIDO, EstadoVenta.CANCELADO));
        assertTrue(TransicionEstadoValidator.esValida(EstadoVenta.EN_PREPARACION, EstadoVenta.CANCELADO));
        assertTrue(TransicionEstadoValidator.esValida(EstadoVenta.LISTO, EstadoVenta.CANCELADO));
    }

    @Test
    void rechazaSaltarsePasosDelCiclo() {
        assertFalse(TransicionEstadoValidator.esValida(EstadoVenta.RECIBIDO, EstadoVenta.LISTO));
        assertFalse(TransicionEstadoValidator.esValida(EstadoVenta.RECIBIDO, EstadoVenta.ENTREGADO));
        assertFalse(TransicionEstadoValidator.esValida(EstadoVenta.EN_PREPARACION, EstadoVenta.ENTREGADO));
    }

    @Test
    void rechazaRetrocederDeEstado() {
        assertFalse(TransicionEstadoValidator.esValida(EstadoVenta.LISTO, EstadoVenta.EN_PREPARACION));
        assertFalse(TransicionEstadoValidator.esValida(EstadoVenta.ENTREGADO, EstadoVenta.RECIBIDO));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoVenta.class, names = {"ENTREGADO", "CANCELADO"})
    void losEstadosTerminalesNoAdmitenNingunaTransicion(EstadoVenta terminal) {
        assertTrue(TransicionEstadoValidator.esTerminal(terminal));
        for (EstadoVenta destino : EstadoVenta.values()) {
            assertFalse(TransicionEstadoValidator.esValida(terminal, destino),
                    terminal + " no deberia poder pasar a " + destino);
        }
    }

    @Test
    void validarLanzaExcepcionConMensajeClaroEnUnaTransicionInvalida() {
        TransicionInvalidaException excepcion = assertThrows(TransicionInvalidaException.class,
                () -> TransicionEstadoValidator.validar(EstadoVenta.RECIBIDO, EstadoVenta.LISTO));
        assertTrue(excepcion.getMessage().contains("RECIBIDO"));
        assertTrue(excepcion.getMessage().contains("LISTO"));
    }

    @Test
    void validarLanzaExcepcionSiElEstadoYaEsElMismo() {
        assertThrows(TransicionInvalidaException.class,
                () -> TransicionEstadoValidator.validar(EstadoVenta.EN_PREPARACION, EstadoVenta.EN_PREPARACION));
    }

    @Test
    void validarLanzaExcepcionSiLaVentaYaEstaEnUnEstadoFinal() {
        assertThrows(TransicionInvalidaException.class,
                () -> TransicionEstadoValidator.validar(EstadoVenta.CANCELADO, EstadoVenta.EN_PREPARACION));
    }

    @Test
    void validarNoLanzaNadaEnUnaTransicionValida() {
        assertDoesNotThrow(() -> TransicionEstadoValidator.validar(EstadoVenta.RECIBIDO, EstadoVenta.EN_PREPARACION));
    }

    @Test
    void unEstadoNoTerminalNuncaSeReportaComoTerminal() {
        assertEquals(false, TransicionEstadoValidator.esTerminal(EstadoVenta.RECIBIDO));
        assertEquals(false, TransicionEstadoValidator.esTerminal(EstadoVenta.EN_PREPARACION));
        assertEquals(false, TransicionEstadoValidator.esTerminal(EstadoVenta.LISTO));
    }
}
