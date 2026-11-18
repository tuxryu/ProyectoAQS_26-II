package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in;

import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

/**
 * Puerto de entrada para el caso de uso de actualizacion de clientes.
 */
public interface ActualizarClienteUseCasePort {

    /**
     * Actualiza la informacion de un cliente existente.
     *
     * @param cliente entidad con los datos actualizados
     * @return cliente actualizado
     */
    Cliente actualizarCliente(Cliente cliente);
}
