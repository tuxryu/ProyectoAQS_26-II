package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.out;

import java.util.List;
import java.util.Optional;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

/**
 * Puerto de salida hacia el Sistema Dueno para las operaciones de lectura y
 * escritura de clientes.
 */
public interface ClienteRemotoPort {

    Optional<Cliente> buscarPorCurp(String curp);

    List<Cliente> listarTodos();

    List<Cliente> buscarPorNombre(
            String nombres, String primerApellido, String segundoApellido);

    Cliente guardar(Cliente cliente);

    Cliente actualizar(Cliente cliente);
}
