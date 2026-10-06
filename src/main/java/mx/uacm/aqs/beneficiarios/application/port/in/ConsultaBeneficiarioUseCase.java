package mx.uacm.aqs.beneficiarios.application.port.in;

import java.util.List;
import mx.uacm.aqs.beneficiarios.domain.model.Beneficiario;

public interface ConsultaBeneficiarioUseCase {

    List<Beneficiario> consultarPorPoliza(String clavePoliza);
}
