package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in;

import java.util.List;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;

/**
 * Puerto de entrada para la consulta de beneficiarios (listado general y por
 * poliza).
 */
public interface ConsultarBeneficiarioUseCasePort {

    /**
     * Lista los beneficiarios registrados para la poliza indicada.
     *
     * @param polizaId identificador UUID de la poliza en texto
     * @return lista de beneficiarios de la poliza
     */
    List<Beneficiario> consultarPorPoliza(String polizaId);

    /**
     * Lista todos los beneficiarios registrados en el sistema.
     *
     * @return lista completa de beneficiarios validos
     */
    default List<Beneficiario> listarTodos() {
        return List.of();
    }
}
