package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in;

import java.util.Optional;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

/**
 * Puerto de entrada para el caso de uso de consulta de cliente por CURP.
 */
public interface ConsultarClienteUseCase {

    /**
     * Consulta un cliente registrado por su CURP.
     *
     * @param curp CURP del cliente a consultar
     * @return Optional con el cliente encontrado o vacio si no existe
     */
    Optional<Cliente> consultarPorCurp(String curp);
}
