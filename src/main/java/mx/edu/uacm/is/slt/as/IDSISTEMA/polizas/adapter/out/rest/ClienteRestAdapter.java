package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.out.rest;

import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultarClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.ClienteServicioPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;

/**
 * Adaptador inter-modulo en memoria que implementa {@link ClienteServicioPort}
 * invocando {@link ConsultarClienteUseCase} del modulo de clientes sin realizar
 * llamadas HTTP internas.
 */
@Component
public class ClienteRestAdapter implements ClienteServicioPort {

    private final ConsultarClienteUseCase consultarClienteUseCase;

    /**
     * Crea el adaptador con el caso de uso de consulta de clientes.
     *
     * @param consultarClienteUseCase puerto de entrada del modulo de clientes
     */
    public ClienteRestAdapter(ConsultarClienteUseCase consultarClienteUseCase) {
        this.consultarClienteUseCase = Objects.requireNonNull(
                consultarClienteUseCase,
                "El caso de uso ConsultarClienteUseCase es obligatorio.");
    }

    /**
     * Constructor para inyeccion de Spring cuando el servicio de clientes es
     * opcional en el contexto.
     *
     * @param consultarClienteUseCase Optional con el caso de uso de clientes
     */
    @Autowired
    public ClienteRestAdapter(Optional<ConsultarClienteUseCase> consultarClienteUseCase) {
        this.consultarClienteUseCase = Objects.requireNonNull(
                consultarClienteUseCase,
                "El contenedor Optional de ConsultarClienteUseCase no puede ser nulo.")
                .orElse(null);
    }

    @Override
    public boolean existeClientePorCurp(String curp) {
        if (curp == null || curp.trim().isEmpty()) {
            return false;
        }
        if (consultarClienteUseCase == null) {
            throw new IllegalStateException(
                    "ConsultarClienteUseCase no esta disponible en el contexto.");
        }
        try {
            Optional<Cliente> resultado = consultarClienteUseCase.consultarPorCurp(curp);
            return resultado != null && resultado.isPresent();
        } catch (RecursoNoEncontradoException ex) {
            return false;
        }
    }
}
