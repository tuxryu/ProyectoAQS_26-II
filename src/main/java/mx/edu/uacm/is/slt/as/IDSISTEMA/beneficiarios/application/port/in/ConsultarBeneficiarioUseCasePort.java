package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in;

import java.util.List;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;

public interface ConsultarBeneficiarioUseCasePort {
    List<Beneficiario> consultarPorPoliza(String polizaId);
}