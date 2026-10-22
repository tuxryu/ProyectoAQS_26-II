package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.inmemory;

import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.ports.in.ConsultarClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.application.port.out.ClienteServicioPort;
import org.springframework.stereotype.Component;

@Component
public class ClienteRestAdapter implements ClienteServicioPort {

    private final ConsultarClienteUseCase consultarClienteUseCase;

    public ClienteRestAdapter(ConsultarClienteUseCase consultarClienteUseCase) {
        this.consultarClienteUseCase = consultarClienteUseCase;
    }

    @Override
    public Cliente obtenerClientePorId(String idCliente) {
        return consultarClienteUseCase.consultarPorId(idCliente);
    }
}