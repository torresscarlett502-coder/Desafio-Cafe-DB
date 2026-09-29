package sv.udb.cafedonbosco.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formatea valores para las vistas JSP con un resultado fijo (punto
 * decimal, dos cifras), sin depender del Accept-Language del navegador.
 * JSTL fmt:formatNumber usa el locale de la peticion, asi que en un
 * navegador en espanol "2.50" se mostraria como "2,50"; con este helper
 * el signo "$" que ya escribimos en el JSP siempre precede un numero con
 * el mismo formato.
 */
public final class FormatoUtil {

    private FormatoUtil() {
    }

    public static String moneda(BigDecimal valor) {
        if (valor == null) {
            return "0.00";
        }
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
