package sv.udb.cafedonbosco.exception;

/**
 * Excepcion base de negocio. Cada subclase representa un tipo de error
 * esperado y lleva el codigo HTTP con el que el servlet debe responder.
 */
public class AppException extends RuntimeException {

    private final int codigoHttp;

    public AppException(String mensaje, int codigoHttp) {
        super(mensaje);
        this.codigoHttp = codigoHttp;
    }

    public int getCodigoHttp() {
        return codigoHttp;
    }
}
