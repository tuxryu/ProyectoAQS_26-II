package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.service;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CancelarPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.PolizaRemotaPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;

/**
 * Caso de uso de cancelacion (eliminacion) de polizas. Verifica antes que la
 * poliza exista (404) y comprueba con GET despues del DELETE que ya no siga
 * registrada en el Sistema Dueno (Hallazgo 11).
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
        if (polizaRemotaPort.buscarPorClave(clave).isEmpty()) {
            throw new PolizaNoEncontradaException(clave);
        }
        polizaRemotaPort.eliminar(clave);
        if (polizaRemotaPort.buscarPorClave(clave).isPresent()) {
            throw new SistemaDuenoRechazoException(
                    "El Sistema Dueno no elimino la poliza con clave " + clave + ".");
        }
    }
}
