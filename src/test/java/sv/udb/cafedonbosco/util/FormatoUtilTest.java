package sv.udb.cafedonbosco.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * FormatoUtil.moneda existe justamente para NO depender del locale del
 * navegador (JSTL fmt:formatNumber si depende de el, y en un navegador en
 * espanol convertiria "2.50" en "2,50"). Estas pruebas fijan ese
 * contrato: siempre punto decimal, siempre dos cifras.
 */
class FormatoUtilTest {

    @Test
    void formateaConDosDecimalesYPuntoDecimal() {
        assertEquals("2.50", FormatoUtil.moneda(new BigDecimal("2.5")));
        assertEquals("10.00", FormatoUtil.moneda(new BigDecimal("10")));
        assertEquals("3.14", FormatoUtil.moneda(new BigDecimal("3.14")));
    }

    @Test
    void redondeaAMitadArribaEnLaTerceraCifra() {
        assertEquals("2.13", FormatoUtil.moneda(new BigDecimal("2.125")));
        assertEquals("2.12", FormatoUtil.moneda(new BigDecimal("2.124")));
    }

    @Test
    void valorNuloSeFormateaComoCero() {
        assertEquals("0.00", FormatoUtil.moneda(null));
    }

    @Test
    void nuncaUsaComaComoSeparadorDecimal() {
        String resultado = FormatoUtil.moneda(new BigDecimal("1234.5"));
        assertEquals("1234.50", resultado);
    }
}
