package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in;

import java.util.UUID;

public interface CancelarPolizaUseCasePort {
    void cancelarPoliza(UUID id);
}