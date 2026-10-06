package mx.uacm.aqs.clientes.application.port.in;

import mx.uacm.aqs.clientes.domain.model.Cliente;

public interface ModificarClienteUseCase {

    Cliente modificarCliente(Cliente cliente);
}
