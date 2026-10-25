package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception;

import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;

/**
 * Excepcion lanzada cuando no existe una poliza con la clave indicada (HTTP 404).
 */
public class PolizaNoEncontradaException extends RecursoNoEncontradoException {

    public PolizaNoEncontradaException(UUID clave) {
        super("No existe la poliza con clave " + clave + ".");
    }
}
