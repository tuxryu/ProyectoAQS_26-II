package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.exception;

/**
 * Excepcion de dominio lanzada cuando la CURP de un cliente es nula, vacia, no
 * tiene 18 caracteres o contiene caracteres prohibidos como diagonales.
 */
public class CurpInvalidaException extends IllegalArgumentException {

    /**
     * Crea una excepcion con el mensaje descriptivo indicado.
     *
     * @param mensaje detalle de la violacion de la CURP
     */
    public CurpInvalidaException(String mensaje) {
        super(mensaje);
    }

    /**
     * Crea una excepcion con mensaje y causa subyacente.
     *
     * @param mensaje detalle de la violacion de la CURP
     * @param causa excepcion origen
     */
    public CurpInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
