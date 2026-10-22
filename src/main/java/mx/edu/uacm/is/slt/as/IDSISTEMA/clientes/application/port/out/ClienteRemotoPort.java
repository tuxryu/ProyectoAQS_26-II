package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.out;

import java.util.Optional;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

/**
 * Puerto de salida hacia el Sistema Dueño para operaciones de Clientes.
 */
public interface ClienteRemotoPort {

    Optional<Cliente> consultarPorCurp(String curp);

    Cliente registrarCliente(Cliente cliente);

    Cliente actualizarCliente(Cliente cliente);
}