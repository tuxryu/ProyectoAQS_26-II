package mx.uacm.aqs.polizas.application.port.in;

import java.util.UUID;

public interface GestionarPolizaUseCase {

    void cancelarPoliza(UUID polizaId);
}
