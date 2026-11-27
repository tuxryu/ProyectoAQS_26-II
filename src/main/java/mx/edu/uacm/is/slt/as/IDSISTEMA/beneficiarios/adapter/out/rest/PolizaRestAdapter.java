package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.out.rest;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out.PolizaServicioPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CancelarPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CrearPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;

/**
 * Adaptador inter-modulo en memoria que implementa {@link PolizaServicioPort}
 * invocando puertos de entrada del modulo de polizas sin realizar llamadas HTTP
 * internas.
 */
@Component
public class PolizaRestAdapter implements PolizaServicioPort {

    private final ConsultaPolizaUseCase consultaPolizaUseCase;

    private final CancelarPolizaUseCasePort cancelarPolizaUseCasePort;

    private final CrearPolizaUseCasePort crearPolizaUseCasePort;

    /**
     * Crea el adaptador con el caso de uso de consulta de polizas.
     *
     * @param consultaPolizaUseCase puerto de entrada de consulta de polizas
     */
    public PolizaRestAdapter(ConsultaPolizaUseCase consultaPolizaUseCase) {
        this(consultaPolizaUseCase, Optional.empty(), Optional.empty());
    }

    /**
     * Crea el adaptador con los puertos de consulta, cancelacion y alta de
     * polizas requeridos para la operacion compuesta de baja y edicion de
     * beneficiarios.
     *
     * @param consultaPolizaUseCase puerto de consulta de polizas
     * @param cancelarPolizaUseCasePort puerto de cancelacion de polizas
     * @param crearPolizaUseCasePort puerto de alta de polizas
     */
    @Autowired
    public PolizaRestAdapter(
            ConsultaPolizaUseCase consultaPolizaUseCase,
            Optional<CancelarPolizaUseCasePort> cancelarPolizaUseCasePort,
            Optional<CrearPolizaUseCasePort> crearPolizaUseCasePort) {
        this.consultaPolizaUseCase = Objects.requireNonNull(
                consultaPolizaUseCase,
                "El caso de uso ConsultaPolizaUseCase es obligatorio.");
        this.cancelarPolizaUseCasePort = Objects.requireNonNull(
                cancelarPolizaUseCasePort,
                "El Optional de CancelarPolizaUseCasePort no puede ser nulo.")
                .orElse(null);
        this.crearPolizaUseCasePort = Objects.requireNonNull(
                crearPolizaUseCasePort,
                "El Optional de CrearPolizaUseCasePort no puede ser nulo.")
                .orElse(null);
    }

    @Override
    public boolean existePolizaPorClave(UUID clave) {
        if (clave == null) {
            return false;
        }
        try {
            return consultaPolizaUseCase.consultarPorClave(clave) != null;
        } catch (PolizaNoEncontradaException ex) {
            return false;
        }
    }

    @Override
    public void reiniciarPolizaConservandoClave(UUID clave) {
        Objects.requireNonNull(clave, "La clave de la poliza es obligatoria.");
        if (cancelarPolizaUseCasePort == null || crearPolizaUseCasePort == null) {
            throw new IllegalStateException(
                    "Los puertos de cancelacion y alta de polizas son obligatorios "
                    + "para reiniciar la poliza.");
        }
        Poliza respaldo = consultaPolizaUseCase.consultarPorClave(clave);
        cancelarPolizaUseCasePort.cancelarPoliza(clave);
        Poliza copia = new Poliza(
                respaldo.getClave(),
                respaldo.getTipo(),
                respaldo.getMonto(),
                respaldo.getDescripcion(),
                respaldo.getCurpCliente());
        crearPolizaUseCasePort.crearPoliza(copia);
    }
}
