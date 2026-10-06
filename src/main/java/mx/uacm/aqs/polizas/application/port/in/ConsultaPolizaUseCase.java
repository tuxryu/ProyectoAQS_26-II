package mx.uacm.aqs.polizas.application.port.in;

import java.util.List;
import mx.uacm.aqs.polizas.domain.model.Poliza;

public interface ConsultaPolizaUseCase {

    List<Poliza> consultarPolizasPorCliente(String clienteCurp);
}
