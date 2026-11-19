package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.exception;

import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.BeneficiarioId;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;

/**
 * Excepcion de dominio lanzada cuando no se encuentra un beneficiario
 * registrado para la poliza y datos indicados (se traduce a HTTP 404).
 */
public class BeneficiarioNoEncontradoException extends RecursoNoEncontradoException {

    /**
     * Crea una excepcion con el mensaje descriptivo indicado.
     *
     * @param mensaje detalle del beneficiario no encontrado
     */
    public BeneficiarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    /**
     * Crea una excepcion a partir del identificador compuesto del beneficiario.
     *
     * @param id identificador compuesto del beneficiario buscado
     */
    public BeneficiarioNoEncontradoException(BeneficiarioId id) {
        super(construirMensaje(id));
    }

    private static String construirMensaje(BeneficiarioId id) {
        if (id == null) {
            return "No se encontro el beneficiario solicitado.";
        }
        return "No se encontro el beneficiario "
                + id.getNombres() + " " + id.getPrimerApellido()
                + " en la poliza " + id.getClavePoliza() + ".";
    }
}
