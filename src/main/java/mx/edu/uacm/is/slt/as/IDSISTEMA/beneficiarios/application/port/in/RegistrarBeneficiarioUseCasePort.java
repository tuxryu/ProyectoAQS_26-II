package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in;

import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;

public interface RegistrarBeneficiarioUseCasePort {
    Beneficiario registrarBeneficiario(Beneficiario beneficiario);
}