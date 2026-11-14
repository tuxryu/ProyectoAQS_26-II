package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in;

import java.util.Optional;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

public interface ConsultarClienteUseCasePort {
    Optional<Cliente> consultarPorCurp(String curp);
}