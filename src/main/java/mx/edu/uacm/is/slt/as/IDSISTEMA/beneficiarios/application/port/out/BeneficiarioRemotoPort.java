package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out;

import java.util.List;
import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;

/**
 * Puerto de salida hacia el Sistema Dueño para operaciones de Beneficiarios.
 */
public interface BeneficiarioRemotoPort {

    List<Beneficiario> consultarPorClavePoliza(UUID clavePoliza);

    Beneficiario registrarBeneficiario(Beneficiario beneficiario);
}