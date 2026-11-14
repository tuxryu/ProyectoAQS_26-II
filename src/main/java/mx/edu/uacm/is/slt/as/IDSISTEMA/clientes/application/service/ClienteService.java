package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultaClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.FiltroCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.out.ClienteRemotoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.exception.ClienteNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Caso de uso de consultas de clientes. Delega la lectura al Sistema Dueño a
 * través de {@link ClienteRemotoPort} y página en memoria, ordenando por CURP
 * para que las páginas sean estables entre llamadas.
 */
@Service
public class ClienteService implements ConsultaClienteUseCase {

    /** Tamaño máximo de página permitido. */
    public static final int TAMANO_MAXIMO = 100;

    private static final Comparator<Cliente> POR_CURP
            = Comparator.comparing(Cliente::getCurp);

    private final ClienteRemotoPort clienteRemotoPort;

    public ClienteService(ClienteRemotoPort clienteRemotoPort) {
        this.clienteRemotoPort = Objects.requireNonNull(
                clienteRemotoPort, "El puerto ClienteRemotoPort es obligatorio.");
    }

    @Override
    public Cliente consultarPorCurp(String curp) {
        if (curp == null || curp.trim().isEmpty()) {
            throw new SolicitudInvalidaException("La CURP del cliente es obligatoria.");
        }
        return clienteRemotoPort.buscarPorCurp(curp)
                .orElseThrow(() -> new ClienteNoEncontradoException(curp));
    }

    @Override
    public Pagina<Cliente> consultar(FiltroCliente filtro, int pagina, int tamano) {
        if (filtro == null) {
            throw new SolicitudInvalidaException("El filtro de la consulta es obligatorio.");
        }
        validarPaginacion(pagina, tamano);
        List<Cliente> ordenados = buscar(filtro).stream()
                .sorted(POR_CURP)
                .toList();
        return Pagina.de(ordenados, pagina, tamano);
    }

    private List<Cliente> buscar(FiltroCliente filtro) {
        if (filtro instanceof FiltroCliente.Todos) {
            return clienteRemotoPort.listarTodos();
        }
        if (filtro instanceof FiltroCliente.PorNombre porNombre) {
            return clienteRemotoPort.buscarPorNombre(
                    porNombre.nombres(),
                    porNombre.primerApellido(),
                    porNombre.segundoApellido());
        }
        throw new IllegalArgumentException("Tipo de filtro de cliente no soportado: " + filtro);
    }

    private void validarPaginacion(int pagina, int tamano) {
        if (pagina < 0) {
            throw new SolicitudInvalidaException(
                    "El parametro pagina debe ser mayor o igual a 0.");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new SolicitudInvalidaException(
                    "El parametro tamano debe estar entre 1 y " + TAMANO_MAXIMO + ".");
        }
    }
}