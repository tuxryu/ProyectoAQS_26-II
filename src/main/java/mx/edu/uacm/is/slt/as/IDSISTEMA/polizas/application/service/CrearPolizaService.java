package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.service;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CrearPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.ClienteServicioPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.PolizaRemotaPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;

/**
 * Servicio de aplicacion para el alta de polizas. Genera UUID cuando no se
 * proporciona, verifica que el titular exista (422), evita sobrescribir una
 * poliza existente (409, Hallazgo 4) y relee con GET tras guardar (Hallazgo 7).
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
        UUID clave = poliza.getClave() != null ? poliza.getClave() : UUID.randomUUID();
        poliza.setClave(clave);

        if (!clienteServicioPort.existeClientePorCurp(poliza.getCurpCliente())) {
            throw new ReglaNegocioException(
                    "No existe un cliente con la CURP " + poliza.getCurpCliente() + ".");
        }
        if (polizaRemotaPort.buscarPorClave(clave).isPresent()) {
            throw new ConflictoDatosException(
                    "Ya existe una poliza registrada con la clave " + clave + ".");
        }
        Poliza guardada = polizaRemotaPort.guardar(poliza);
        return polizaRemotaPort.buscarPorClave(clave).orElse(guardada);
    }
}
