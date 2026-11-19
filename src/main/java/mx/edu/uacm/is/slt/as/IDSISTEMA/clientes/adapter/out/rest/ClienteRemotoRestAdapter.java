package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest.dto.ClienteRemotoDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.out.ClienteRemotoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http.FechasSistemaDueno;

/**
 * Adaptador de salida REST que implementa {@link ClienteRemotoPort} delegando
 * en {@link ClienteSistemaDuenoAdapter} y mapeando a la entidad {@link Cliente}.
 */
@Component
public class ClienteRemotoRestAdapter implements ClienteRemotoPort {

    private final ClienteSistemaDuenoAdapter clienteSistemaDuenoAdapter;

    /**
     * Crea el adaptador con el cliente hacia el Sistema Dueno.
     *
     * @param clienteSistemaDuenoAdapter adaptador HTTP del Sistema Dueno
     */
    @Autowired
    public ClienteRemotoRestAdapter(
            ClienteSistemaDuenoAdapter clienteSistemaDuenoAdapter) {
        this.clienteSistemaDuenoAdapter = Objects.requireNonNull(
                clienteSistemaDuenoAdapter,
                "El adaptador ClienteSistemaDuenoAdapter es obligatorio.");
    }

    /**
     * Constructor compatible con inyeccion de {@link RestClient}.
     *
     * @param clienteSistemaDuenoAdapter adaptador HTTP del Sistema Dueno
     * @param restClient cliente HTTP (conservado por compatibilidad)
     */
    public ClienteRemotoRestAdapter(
            ClienteSistemaDuenoAdapter clienteSistemaDuenoAdapter,
            RestClient restClient) {
        this(clienteSistemaDuenoAdapter);
        Objects.requireNonNull(
                restClient, "El cliente HTTP RestClient es obligatorio.");
    }

    @Override
    public Optional<Cliente> buscarPorCurp(String curp) {
        return clienteSistemaDuenoAdapter.buscarPorCurp(curp)
                .map(this::convertirADominio);
    }

    @Override
    public List<Cliente> listarTodos() {
        return clienteSistemaDuenoAdapter.listarTodos();
    }

    @Override
    public List<Cliente> buscarPorNombre(
            String nombres, String primerApellido, String segundoApellido) {
        return clienteSistemaDuenoAdapter.buscarPorNombre(
                nombres, primerApellido, segundoApellido);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        Objects.requireNonNull(cliente, "El cliente a guardar es obligatorio.");
        ClienteRemotoDto dto = convertirADto(cliente);
        return convertirADominio(clienteSistemaDuenoAdapter.guardar(dto));
    }

    @Override
    public Cliente actualizar(Cliente cliente) {
        Objects.requireNonNull(cliente, "El cliente a actualizar es obligatorio.");
        ClienteRemotoDto dto = convertirADto(cliente);
        return convertirADominio(
                clienteSistemaDuenoAdapter.actualizar(cliente.getCurp(), dto));
    }

    private Cliente convertirADominio(ClienteRemotoDto dto) {
        return new Cliente(
                dto.getCurp(),
                dto.getNombres(),
                dto.getPrimerApellido(),
                dto.getSegundoApellido(),
                dto.getDireccion(),
                dto.obtenerFechaNacimientoLocal());
    }

    private ClienteRemotoDto convertirADto(Cliente cliente) {
        return new ClienteRemotoDto(
                cliente.getCurp(),
                cliente.getNombres(),
                cliente.getPrimerApellido(),
                cliente.getSegundoApellido(),
                cliente.getDireccion(),
                FechasSistemaDueno.formatearParaRuta(cliente.getFechaNacimiento()));
    }
}
