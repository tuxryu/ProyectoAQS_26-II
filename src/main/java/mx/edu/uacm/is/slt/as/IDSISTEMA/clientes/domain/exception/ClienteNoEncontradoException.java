package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.exception;

import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;

/**
 * Excepcion de dominio lanzada cuando no existe un cliente registrado con la
 * CURP indicada (se traduce a HTTP 404).
 */
public class ClienteNoEncontradoException extends RecursoNoEncontradoException {

    /**
     * Crea una excepcion indicando la CURP del cliente no encontrado.
     *
     * @param curp CURP del cliente buscado
     */
    public ClienteNoEncontradoException(String curp) {
        super("No existe el cliente con CURP " + curp + ".");
    }
}
