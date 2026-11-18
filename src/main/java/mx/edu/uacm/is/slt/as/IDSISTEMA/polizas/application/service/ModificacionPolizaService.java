package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.service;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ModificacionPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.ClienteServicioPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.PolizaRemotaPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;

/**
 * Servicio de aplicacion para la modificacion de polizas. Verifica que la
 * poliza exista previamente (404, Hallazgo 9), valida al titular cuando cambia
 * la CURP (422) y relee con GET tras actualizar (Hallazgo 7).
 */
@Service
public class ModificacionPolizaService implements ModificacionPolizaUseCasePort {

    private final PolizaRemotaPort polizaRemotaPort;

    private final ClienteServicioPort clienteServicioPort;

    public ModificacionPolizaService(
            PolizaRemotaPort polizaRemotaPort,
            ClienteServicioPort clienteServicioPort) {
        this.polizaRemotaPort = Objects.requireNonNull(
                polizaRemotaPort, "El puerto PolizaRemotaPort es obligatorio.");
        this.clienteServicioPort = Objects.requireNonNull(
                clienteServicioPort, "El puerto ClienteServicioPort es obligatorio.");
    }

    @Override
    public Poliza modificarPoliza(UUID clave, Poliza polizaConCambios) {
        if (clave == null) {
            throw new SolicitudInvalidaException("La clave de la poliza es obligatoria.");
        }
        if (polizaConCambios == null) {
            throw new SolicitudInvalidaException("Los datos de la poliza son obligatorios.");
        }
        Poliza actual = polizaRemotaPort.buscarPorClave(clave)
                .orElseThrow(() -> new PolizaNoEncontradaException(clave));

        if (!Objects.equals(actual.getCurpCliente(), polizaConCambios.getCurpCliente())
                && !clienteServicioPort.existeClientePorCurp(polizaConCambios.getCurpCliente())) {
            throw new ReglaNegocioException(
                    "No existe un cliente con la CURP "
                            + polizaConCambios.getCurpCliente() + ".");
        }
        Poliza actualizada = polizaRemotaPort.actualizar(clave, polizaConCambios);
        return polizaRemotaPort.buscarPorClave(clave).orElse(actualizada);
    }
}
