package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception;

/**
 * Excepcion para solicitudes mal formadas o incompletas (se traduce a HTTP 400),
 * por ejemplo mas de un filtro en la misma consulta o paginacion invalida.
 */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
