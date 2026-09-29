package sv.udb.cafedonbosco.util;

import java.math.BigDecimal;
import java.util.Set;

public final class Constantes {

    private Constantes() {
    }

    // Atributos de sesion HTTP
    public static final String SESSION_USUARIO = "usuarioAutenticado";
    public static final String SESSION_CARRITO = "carrito";
    // Carrito de la venta presencial (POS del administrador); se guarda
    // separado del carrito del consumidor para que compartir el mismo
    // navegador de pruebas no mezcle una venta de mostrador con una compra
    // web.
    public static final String SESSION_CARRITO_ADMIN = "carritoAdmin";
    // Clave de idempotencia del intento de checkout en curso: se genera al
    // mostrar el formulario y se reutiliza en reintentos de la misma
    // sesion (p. ej. doble clic o un reenvio accidental) para que el
    // backend pueda detectar y descartar una venta duplicada.
    public static final String SESSION_CHECKOUT_IDEMPOTENCY = "checkoutIdempotencyKey";

    // Cabecera de respuesta JSON
    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    // Reglas de negocio
    public static final int STOCK_MINIMO_POR_DEFECTO = 5;

    public static final String ENTREGA_RECOGER = "RECOGER";
    public static final String ENTREGA_DOMICILIO = "DOMICILIO";

    /** Tarifa fija de envio a domicilio; recoger en tienda siempre es 0. */
    public static final BigDecimal TARIFA_ENVIO_DOMICILIO = new BigDecimal("1.50");

    /** Limite maximo para "size"/"limite" en listados paginados. */
    public static final int TAMANO_PAGINA_MAXIMO = 100;
    public static final int TAMANO_PAGINA_POR_DEFECTO = 20;

    /**
     * Origenes autorizados a hacer solicitudes CORS con credenciales
     * (cookie de sesion) contra la API. En produccion la app se sirve
     * desde el mismo origen que consume la API, asi que esto solo hace
     * falta para herramientas de desarrollo (un frontend servido aparte
     * en local); nunca se debe reflejar un Origin arbitrario, porque con
     * Access-Control-Allow-Credentials en true eso le permitiria a
     * cualquier sitio web hacer solicitudes autenticadas usando la cookie
     * de sesion de la victima.
     */
    public static final Set<String> ORIGENES_CORS_PERMITIDOS = Set.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:8080",
            "http://127.0.0.1:8080"
    );
}
