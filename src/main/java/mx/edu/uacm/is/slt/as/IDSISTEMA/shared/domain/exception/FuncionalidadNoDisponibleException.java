package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception;

/**
 * Excepcion para funcionalidades previstas pero aun no implementadas (se
 * traduce a HTTP 501), por ejemplo un filtro cuyo endpoint remoto no se ha
 * identificado en el catalogo del Sistema Dueno.
 */
public class FuncionalidadNoDisponibleException extends RuntimeException {

    public FuncionalidadNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
