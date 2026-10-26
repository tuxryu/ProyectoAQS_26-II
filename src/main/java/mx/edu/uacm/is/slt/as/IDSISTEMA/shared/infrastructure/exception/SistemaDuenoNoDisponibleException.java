package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception;

/**
 * Excepcion lanzada cuando el Sistema Dueno no esta disponible (HTTP 502, 503,
 * 504, timeout o fallo de red).
 */
public class SistemaDuenoNoDisponibleException extends RuntimeException {

    public SistemaDuenoNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public SistemaDuenoNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
