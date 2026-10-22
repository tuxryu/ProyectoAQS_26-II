package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in;

import java.util.List;
import java.util.Optional;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;

public interface ConsultarPolizaUseCasePort {
    Optional<Poliza> consultarPorId(String id);
    List<Poliza> consultarPorCliente(String curpCliente);
}