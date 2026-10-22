package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.application.port.out;

import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

public interface ClienteServicioPort {
    Cliente obtenerClientePorId(String idCliente);
}