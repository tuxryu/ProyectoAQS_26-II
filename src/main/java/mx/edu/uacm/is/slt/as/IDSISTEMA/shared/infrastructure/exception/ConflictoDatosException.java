
package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception;

public class ConflictoDatosException extends RuntimeException {

    public ConflictoDatosException(String mensaje) {
        super(mensaje);
    }

    public ConflictoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
