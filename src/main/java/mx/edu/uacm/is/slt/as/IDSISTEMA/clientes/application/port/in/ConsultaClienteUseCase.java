package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in;

import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Puerto de entrada para las consultas de clientes (lectura por CURP y
 * listados con filtro y paginación).
 */
public interface ConsultaClienteUseCase {

    /**
     * Consulta un cliente por su CURP.
     *
     * @param curp CURP del cliente
     * @return el cliente encontrado
     *         si no existe
     */
    Cliente consultarPorCurp(String curp);

    /**
     * Lista los clientes que cumplen el filtro, paginados y ordenados.
     *
     * @param filtro filtro de la consulta ({@link FiltroCliente.Todos} para no filtrar)
     * @param pagina número de página, empezando en 0
     * @param tamano elementos por página
     * @return página de clientes; puede venir vacía
     */
    Pagina<Cliente> consultar(FiltroCliente filtro, int pagina, int tamano);
}