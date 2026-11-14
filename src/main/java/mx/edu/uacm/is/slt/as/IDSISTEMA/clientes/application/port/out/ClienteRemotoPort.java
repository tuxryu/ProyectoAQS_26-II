package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.out;

import java.util.List;
import java.util.Optional;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

/**
 * Puerto de salida hacia el Sistema Dueño para las lecturas de clientes.
 * Las operaciones de escritura (guardar, actualizar, eliminar) se agregarán con
 * sus casos de uso.
 */
public interface ClienteRemotoPort {

    Optional<Cliente> buscarPorCurp(String curp);

    List<Cliente> listarTodos();

    List<Cliente> buscarPorNombre(
            String nombres, String primerApellido, String segundoApellido);
}