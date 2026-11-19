package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.service;

import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultarClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultarClienteUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.out.ClienteRemotoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

/**
 * Servicio de consulta opcional de cliente por CURP que implementa
 * {@link ConsultarClienteUseCase} y {@link ConsultarClienteUseCasePort} para la
 * comunicacion inter-modulo en memoria desde el modulo de polizas.
 */
@Service
public class ConsultarClientePorCurpService
        implements ConsultarClienteUseCase, ConsultarClienteUseCasePort {

    private final ClienteRemotoPort clienteRemotoPort;

    public ConsultarClientePorCurpService(ClienteRemotoPort clienteRemotoPort) {
        this.clienteRemotoPort = Objects.requireNonNull(
                clienteRemotoPort, "El puerto ClienteRemotoPort es obligatorio.");
    }

    @Override
    public Optional<Cliente> consultarPorCurp(String curp) {
        if (curp == null || curp.trim().isEmpty()) {
            return Optional.empty();
        }
        return clienteRemotoPort.buscarPorCurp(curp);
    }
}
