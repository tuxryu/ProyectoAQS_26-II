package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in;

import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

/**
 * Puerto de entrada para el caso de uso de registro de clientes.
 */
public interface RegistrarClienteUseCasePort {

    /**
     * Registra un nuevo cliente en el sistema.
     *
     * @param cliente entidad del cliente a registrar
     * @return cliente registrado
     */
    Cliente registrarCliente(Cliente cliente);
}
