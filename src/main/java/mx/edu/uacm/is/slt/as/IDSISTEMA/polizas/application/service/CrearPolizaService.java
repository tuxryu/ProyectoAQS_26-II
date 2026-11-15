package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.service;

import java.util.Objects;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CrearPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.ClienteServicioPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.PolizaRemotaPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;

/**
 * Caso de uso de creacion (alta) de polizas.
 */
@Service
public class CrearPolizaService implements CrearPolizaUseCasePort {

    private final PolizaRemotaPort polizaRemotaPort;
    private final ClienteServicioPort clienteServicioPort;

    public CrearPolizaService(
            PolizaRemotaPort polizaRemotaPort,
            ClienteServicioPort clienteServicioPort) {
        this.polizaRemotaPort = Objects.requireNonNull(
                polizaRemotaPort, "El puerto PolizaRemotaPort es obligatorio.");
        this.clienteServicioPort = Objects.requireNonNull(
                clienteServicioPort, "El puerto ClienteServicioPort es obligatorio.");
    }

    @Override
    public Poliza crearPoliza(Poliza poliza) {
        if (poliza == null) {
            throw new SolicitudInvalidaException("La poliza es obligatoria.");
        }
        if (poliza.getClave() == null) {
            throw new SolicitudInvalidaException("La clave de la poliza es obligatoria.");
        }
        if (!clienteServicioPort.existeClientePorCurp(poliza.getCurpCliente())) {
            throw new ReglaNegocioException(
                    "No existe un cliente con la CURP " + poliza.getCurpCliente() + ".");
        }
        return polizaRemotaPort.guardar(poliza);
    }
}
