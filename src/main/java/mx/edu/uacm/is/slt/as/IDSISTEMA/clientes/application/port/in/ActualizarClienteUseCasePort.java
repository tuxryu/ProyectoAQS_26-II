package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in;

import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

public interface ActualizarClienteUseCasePort {
    Cliente actualizarCliente(Cliente cliente);
}