package mx.uacm.aqs.beneficiarios.application.port.in;

import mx.uacm.aqs.beneficiarios.domain.model.Beneficiario;

public interface AltaBeneficiarioUseCase {

    Beneficiario altaBeneficiario(Beneficiario beneficiario);
}
