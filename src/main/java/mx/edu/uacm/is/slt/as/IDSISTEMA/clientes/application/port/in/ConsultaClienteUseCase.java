package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in;

import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Puerto de entrada para las consultas de clientes (lectura por CURP y
 * listados con filtro y paginacion).
 */
public interface ConsultaClienteUseCase {

    /**
     * Consulta un cliente por su CURP.
     *
     * @param curp CURP del cliente
     * @return el cliente encontrado
     */
    Cliente consultarPorCurp(String curp);

    /**
     * Lista los clientes que cumplen el filtro, paginados y ordenados.
     *
     * @param filtro filtro de la consulta ({@link FiltroCliente.Todos} para no filtrar)
     * @param pagina numero de pagina, empezando en 0
     * @param tamano elementos por pagina
     * @return pagina de clientes; puede venir vacia
     */
    Pagina<Cliente> consultar(FiltroCliente filtro, int pagina, int tamano);
}
