package sv.udb.cafedonbosco.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {"a@b.com", "usuario.nombre@dominio.co", "a_b-c@sub.dominio.com", "juan@mail.udb.edu.sv"})
    void aceptaCorreosBienFormadosIncluyendoSubdominios(String correo) {
        // "mail.udb.edu.sv" tiene tres subdominios antes del TLD: un patron
        // anterior de PATRON_CORREO solo aceptaba un dominio con un unico
        // punto y rechazaba correos institucionales reales como este.
        assertTrue(ValidacionUtil.esCorreoValido(correo));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sin-arroba.com", "@sindominio.com", "usuario@", "usuario@dominio", "con espacio@dominio.com"})
    void rechazaCorreosMalFormados(String correo) {
        assertFalse(ValidacionUtil.esCorreoValido(correo));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rechazaCorreoNuloOVacio(String correo) {
        assertFalse(ValidacionUtil.esCorreoValido(correo));
    }

    @Test
    void rechazaCorreoDemasiadoLargo() {
        String local = "a".repeat(115);
        String correoLargo = local + "@b.com";
        assertTrue(correoLargo.length() > 120);
        assertFalse(ValidacionUtil.esCorreoValido(correoLargo));
    }

    @ParameterizedTest
    @ValueSource(strings = {"7000-0000", "+503 7000 0000", "70000000"})
    void aceptaTelefonosBienFormados(String telefono) {
        assertTrue(ValidacionUtil.esTelefonoValido(telefono));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "123"})
    void rechazaTelefonosMalFormados(String telefono) {
        assertFalse(ValidacionUtil.esTelefonoValido(telefono));
    }

    @Test
    void esTextoValidoRechazaBlancosYNulos() {
        assertFalse(ValidacionUtil.esTextoValido(null));
        assertFalse(ValidacionUtil.esTextoValido(""));
        assertFalse(ValidacionUtil.esTextoValido("   "));
        assertTrue(ValidacionUtil.esTextoValido("Cafe Latte"));
    }

    @Test
    void esTextoValidoRespetaLaLongitudMaxima() {
        assertTrue(ValidacionUtil.esTextoValido("12345", 5));
        assertFalse(ValidacionUtil.esTextoValido("123456", 5));
    }

    @Test
    void esCantidadValidaSoloAceptaEnterosPositivos() {
        assertTrue(ValidacionUtil.esCantidadValida(1));
        assertTrue(ValidacionUtil.esCantidadValida(20));
        assertFalse(ValidacionUtil.esCantidadValida(0));
        assertFalse(ValidacionUtil.esCantidadValida(-1));
        assertFalse(ValidacionUtil.esCantidadValida(null));
    }
}
