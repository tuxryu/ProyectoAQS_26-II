package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception;

/**
 * Excepcion para violaciones de reglas de negocio o de dominio (se traduce a
 * HTTP 422), por ejemplo un tipo de poliza fuera de rango o un texto con / o \.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
