package mx.uacm.aqs.clientes.application.port.out;

import mx.uacm.aqs.clientes.domain.model.Cliente;

public interface ClienteRemotoPort {

    Cliente guardar(Cliente cliente);

    Cliente consultarPorCurp(String curp);
}
