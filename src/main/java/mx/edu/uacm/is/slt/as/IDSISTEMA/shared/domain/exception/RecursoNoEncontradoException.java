package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception;

/**
 * Excepcion base para recursos inexistentes (se traduce a HTTP 404). Cada
 * modulo la especializa, por ejemplo PolizaNoEncontradaException.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
