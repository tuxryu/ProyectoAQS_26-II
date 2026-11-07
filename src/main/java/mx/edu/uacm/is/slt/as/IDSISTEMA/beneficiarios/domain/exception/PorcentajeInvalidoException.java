package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.exception;

/**
 * Excepcion de dominio lanzada cuando el porcentaje asignado a un beneficiario
 * esta fuera del rango permitido (1 a 100) o cuando la suma de porcentajes de
 * una poliza excede el 100% (se traduce a HTTP 422).
 */
public class PorcentajeInvalidoException extends IllegalArgumentException {

    /**
     * Crea una excepcion con el mensaje descriptivo indicado.
     *
     * @param mensaje detalle de la violacion de porcentaje
     */
    public PorcentajeInvalidoException(String mensaje) {
        super(mensaje);
    }

    /**
     * Crea una excepcion indicando el valor de porcentaje fuera del rango 1..100.
     *
     * @param porcentaje valor invalido recibido
     */
    public PorcentajeInvalidoException(int porcentaje) {
        super("El porcentaje debe estar entre 1 y 100, se recibio: "
                + porcentaje + ".");
    }

    /**
     * Crea una excepcion con mensaje y causa subyacente.
     *
     * @param mensaje detalle de la violacion de porcentaje
     * @param causa excepcion origen
     */
    public PorcentajeInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
