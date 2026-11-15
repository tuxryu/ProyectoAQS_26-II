package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.service;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CancelarPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.PolizaRemotaPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;

/**
 * Caso de uso de cancelacion (eliminacion) de polizas.
 */
@Service
public class CancelarPolizaService implements CancelarPolizaUseCasePort {

    private final PolizaRemotaPort polizaRemotaPort;

    public CancelarPolizaService(PolizaRemotaPort polizaRemotaPort) {
        this.polizaRemotaPort = Objects.requireNonNull(
                polizaRemotaPort, "El puerto PolizaRemotaPort es obligatorio.");
    }

    @Override
    public void cancelarPoliza(UUID clave) {
        if (clave == null) {
            throw new SolicitudInvalidaException("La clave de la poliza es obligatoria.");
        }
        polizaRemotaPort.eliminar(clave);
    }
}
