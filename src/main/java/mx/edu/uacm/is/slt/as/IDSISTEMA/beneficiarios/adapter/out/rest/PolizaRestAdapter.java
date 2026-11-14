package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.out.rest;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out.PolizaServicioPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException;

/**
 * Adaptador inter-modulo en memoria que implementa {@link PolizaServicioPort}
 * invocando {@link ConsultaPolizaUseCase} del modulo de polizas sin realizar
 * llamadas HTTP internas.
 */
@Component
public class PolizaRestAdapter implements PolizaServicioPort {

    private final ConsultaPolizaUseCase consultaPolizaUseCase;

    /**
     * Crea el adaptador con el caso de uso de consulta de polizas.
     *
     * @param consultaPolizaUseCase puerto de entrada del modulo de polizas
     */
    public PolizaRestAdapter(ConsultaPolizaUseCase consultaPolizaUseCase) {
        this.consultaPolizaUseCase = Objects.requireNonNull(
                consultaPolizaUseCase,
                "El caso de uso ConsultaPolizaUseCase es obligatorio.");
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
}
