package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception;

/**
 * Excepcion lanzada cuando el Sistema Dueno rechaza una operacion (HTTP 500 por
 * datos invalidos o respuesta 200 vacia en escritura).
 */
public class SistemaDuenoRechazoException extends RuntimeException {

    public SistemaDuenoRechazoException(String mensaje) {
        super(mensaje);
    }

    public SistemaDuenoRechazoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
