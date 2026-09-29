package sv.udb.cafedonbosco.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.model.Carrito;
import sv.udb.cafedonbosco.model.Rol;

import java.util.UUID;

public final class SessionUtil {

    private SessionUtil() {
    }

    public static UsuarioResponseDTO obtenerUsuarioAutenticado(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return null;
        }
        return (UsuarioResponseDTO) sesion.getAttribute(Constantes.SESSION_USUARIO);
    }

    public static boolean tieneRol(HttpServletRequest request, Rol rol) {
        UsuarioResponseDTO usuario = obtenerUsuarioAutenticado(request);
        return usuario != null && usuario.getRol() == rol;
    }

    public static Carrito obtenerOCrearCarrito(HttpServletRequest request) {
        return obtenerOCrearCarrito(request, Constantes.SESSION_CARRITO);
    }

    public static Carrito obtenerOCrearCarritoAdmin(HttpServletRequest request) {
        return obtenerOCrearCarrito(request, Constantes.SESSION_CARRITO_ADMIN);
    }

    private static Carrito obtenerOCrearCarrito(HttpServletRequest request, String atributo) {
        HttpSession sesion = request.getSession(true);
        Carrito carrito = (Carrito) sesion.getAttribute(atributo);
        if (carrito == null) {
            carrito = new Carrito();
            sesion.setAttribute(atributo, carrito);
        }
        return carrito;
    }

    /**
     * Genera una clave de idempotencia nueva para un intento de checkout y
     * la guarda en la sesion. Se llama al mostrar el formulario (no en
     * cada reintento tras un error de validacion), de forma que un doble
     * envio del mismo formulario viaje siempre con la misma clave.
     */
    public static String generarNuevaClaveCheckout(HttpServletRequest request) {
        String clave = UUID.randomUUID().toString();
        request.getSession(true).setAttribute(Constantes.SESSION_CHECKOUT_IDEMPOTENCY, clave);
        return clave;
    }

    /** Clave de idempotencia del intento de checkout en curso, si existe. */
    public static String obtenerClaveCheckout(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion != null ? (String) sesion.getAttribute(Constantes.SESSION_CHECKOUT_IDEMPOTENCY) : null;
    }

    /** Se llama tras procesar el checkout (con exito o no) para que el siguiente intento use una clave distinta. */
    public static void limpiarClaveCheckout(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.removeAttribute(Constantes.SESSION_CHECKOUT_IDEMPOTENCY);
        }
    }
}
