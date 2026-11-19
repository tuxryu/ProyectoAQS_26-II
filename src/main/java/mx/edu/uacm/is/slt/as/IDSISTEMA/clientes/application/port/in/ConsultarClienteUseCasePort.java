package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in;

import java.util.Optional;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

/**
 * Puerto de entrada para la consulta opcional de un cliente por su CURP.
 */
public interface ConsultarClienteUseCasePort {

    /**
     * Consulta un cliente por su CURP.
     *
     * @param curp CURP del cliente
     * @return Optional con el cliente encontrado o vacio si no existe
     */
    Optional<Cliente> consultarPorCurp(String curp);
}
