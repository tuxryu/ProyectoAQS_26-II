package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception;

/**
 * Excepcion de dominio lanzada cuando el monto de cobertura de una poliza es
 * menor o igual a cero o no es un valor numerico valido (se traduce a HTTP 422).
 */
public class MontoInvalidoException extends IllegalArgumentException {

    /**
     * Crea una excepcion con el mensaje descriptivo indicado.
     *
     * @param mensaje detalle de la violacion del monto
     */
    public MontoInvalidoException(String mensaje) {
        super(mensaje);
    }

    /**
     * Crea una excepcion indicando el monto invalido recibido.
     *
     * @param monto valor de monto menor o igual a cero
     */
    public MontoInvalidoException(float monto) {
        super("El monto de la poliza debe ser mayor a cero, se recibio: "
                + monto + ".");
    }

    /**
     * Crea una excepcion con mensaje y causa subyacente.
     *
     * @param mensaje detalle de la violacion del monto
     * @param causa excepcion origen
     */
    public MontoInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
