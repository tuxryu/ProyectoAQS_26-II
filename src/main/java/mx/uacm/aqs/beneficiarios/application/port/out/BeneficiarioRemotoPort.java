package mx.uacm.aqs.beneficiarios.application.port.out;

import java.util.List;
import mx.uacm.aqs.beneficiarios.domain.model.Beneficiario;

public interface BeneficiarioRemotoPort {

    Beneficiario guardar(Beneficiario beneficiario);

    List<Beneficiario> consultarPorPoliza(String clavePoliza);
}
