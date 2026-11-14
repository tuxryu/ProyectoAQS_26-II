package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest.dto.ClienteRemotoDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.out.ClienteRemotoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http.TraductorExcepcionesSistemaDueno;

/**
 * Adaptador de salida REST que implementa {@link ClienteRemotoPort} delegando
 * en {@link ClienteSistemaDuenoAdapter} y mapeando a la entidad {@link Cliente}.
 */
@Component
public class ClienteRemotoRestAdapter implements ClienteRemotoPort {

    private static final Logger LOG
            = LoggerFactory.getLogger(ClienteRemotoRestAdapter.class);

    private final ClienteSistemaDuenoAdapter clienteSistemaDuenoAdapter;
    private final RestClient restClient;

    public ClienteRemotoRestAdapter(
            ClienteSistemaDuenoAdapter clienteSistemaDuenoAdapter,
            RestClient restClient) {
        this.clienteSistemaDuenoAdapter = Objects.requireNonNull(
                clienteSistemaDuenoAdapter,
                "El adaptador ClienteSistemaDuenoAdapter es obligatorio.");
        this.restClient = Objects.requireNonNull(
                restClient, "El cliente HTTP RestClient es obligatorio.");
    }

    @Override
    public Optional<Cliente> buscarPorCurp(String curp) {
        return clienteSistemaDuenoAdapter.buscarPorCurp(curp)
                .map(this::convertirADominio);
    }

    @Override
    public List<Cliente> listarTodos() {
        return consultarLista("/clientes");
    }

    @Override
    public List<Cliente> buscarPorNombre(
            String nombres, String primerApellido, String segundoApellido) {
        validarSegmentoObligatorio(nombres, "El nombre");
        validarSegmentoObligatorio(primerApellido, "El primer apellido");
        if (segundoApellido == null || segundoApellido.trim().isEmpty()) {
            return consultarLista(
                    "/clientes/{nombres}/{primerApellido}",
                    nombres,
                    primerApellido);
        }
        validarSinDiagonales(segundoApellido, "El segundo apellido");
        return consultarLista(
                "/clientes/{nombres}/{primerApellido}/{segundoApellido}",
                nombres,
                primerApellido,
                segundoApellido);
    }

    private List<Cliente> consultarLista(String plantilla, Object... variables) {
        ClienteRemotoDto[] respuesta = TraductorExcepcionesSistemaDueno.ejecutar(
                () -> restClient.get()
                        .uri(plantilla, variables)
                        .retrieve()
                        .body(ClienteRemotoDto[].class));
        if (respuesta == null || respuesta.length == 0) {
            return List.of();
        }
        List<Cliente> clientes = new ArrayList<>(respuesta.length);
        for (ClienteRemotoDto dto : Arrays.asList(respuesta)) {
            if (dto != null) {
                try {
                    clientes.add(convertirADominio(dto));
                } catch (RuntimeException ex) {
                    LOG.warn("Se omite un cliente invalido del Sistema Dueno: {}",
                            ex.getMessage());
                }
            }
        }
        return List.copyOf(clientes);
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

    private void validarSegmentoObligatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    campo + " no puede ser nulo ni vacio.");
        }
        validarSinDiagonales(valor, campo);
    }

    private void validarSinDiagonales(String valor, String campo) {
        if (valor.contains("/") || valor.contains("\\")) {
            throw new IllegalArgumentException(
                    campo + " no puede contener diagonales.");
        }
    }
}
