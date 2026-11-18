package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest;

import java.time.LocalDate;
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
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http.FechasSistemaDueno;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http.TraductorExcepcionesSistemaDueno;

/**
 * Adaptador de salida REST hacia el Sistema Dueno para operaciones de clientes
 * (GET, POST y PUT), manejando respuestas 200 vacias, errores HTTP y parseo de
 * fechas UTC-6 con FechasSistemaDueno.
 */
@Component
public class ClienteSistemaDuenoAdapter {

    private static final Logger LOG
            = LoggerFactory.getLogger(ClienteSistemaDuenoAdapter.class);

    private static final String RUTA_CON_SEGUNDO_APELLIDO
            = "/cliente/{curp}/{direccion}/{fecha}/{nombres}/{primerApellido}/{segundoApellido}";

    private static final String RUTA_SIN_SEGUNDO_APELLIDO
            = "/cliente/{curp}/{direccion}/{fecha}/{nombres}/{primerApellido}";

    private final RestClient restClient;

    public ClienteSistemaDuenoAdapter(RestClient restClient) {
        this.restClient = Objects.requireNonNull(
                restClient, "El cliente HTTP RestClient es obligatorio.");
    }

    /**
     * Consulta un cliente en el Sistema Dueno por su CURP tal cual (respetando
     * mayusculas y minusculas). Si el servidor responde 200 con cuerpo vacio,
     * devuelve Optional.empty().
     *
     * @param curp CURP del cliente a consultar
     * @return Optional con los datos del cliente o Optional.empty() si no existe
     */
    public Optional<ClienteRemotoDto> buscarPorCurp(String curp) {
        validarSegmentoObligatorio(curp, "La CURP");
        ClienteRemotoDto respuesta = TraductorExcepcionesSistemaDueno.ejecutar(
                () -> restClient.get()
                        .uri("/cliente/{curp}", curp)
                        .retrieve()
                        .body(ClienteRemotoDto.class));

        if (esRespuestaVacia(respuesta)) {
            return Optional.empty();
        }
        FechasSistemaDueno.convertirALocalDate(respuesta.getFechaNacimiento());
        return Optional.of(respuesta);
    }

    /**
     * Lista todos los clientes registrados en el Sistema Dueno mediante el
     * punto de acceso GET /clientes, omitiendo registros invalidos o corruptos
     * de otros equipos.
     *
     * @return lista inmutable de entidades {@link Cliente} validas
     */
    public List<Cliente> listarTodos() {
        return consultarLista("/clientes");
    }

    /**
     * Busca clientes en el Sistema Dueno por su nombre y apellidos
     * (GET /clientes/{nombres}/{primerApellido}[/{segundoApellido}]),
     * omitiendo registros invalidos de otros equipos.
     *
     * @param nombres nombres del cliente
     * @param primerApellido primer apellido del cliente
     * @param segundoApellido segundo apellido del cliente (opcional)
     * @return lista inmutable de entidades {@link Cliente} validas
     */
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

    /**
     * Registra un cliente en el Sistema Dueno mediante POST enviando los
     * atributos como segmentos de ruta codificados. Si responde 200 vacio,
     * lanza SistemaDuenoRechazoException.
     *
     * @param cliente datos del cliente a guardar
     * @return DTO devuelto por el Sistema Dueno
     */
    public ClienteRemotoDto guardar(ClienteRemotoDto cliente) {
        Objects.requireNonNull(cliente, "Los datos del cliente son obligatorios.");
        return enviarEscritura(cliente.getCurp(), cliente, false);
    }

    /**
     * Actualiza un cliente en el Sistema Dueno. Usa PUT cuando tiene segundo
     * apellido y POST cuando no tiene segundo apellido (hallazgo 8).
     *
     * @param curp CURP del cliente a actualizar
     * @param cliente datos actualizados del cliente
     * @return DTO devuelto por el Sistema Dueno
     */
    public ClienteRemotoDto actualizar(String curp, ClienteRemotoDto cliente) {
        Objects.requireNonNull(cliente, "Los datos del cliente son obligatorios.");
        boolean usarPut = cliente.getSegundoApellido() != null;
        return enviarEscritura(curp, cliente, usarPut);
    }

    private ClienteRemotoDto enviarEscritura(
            String curp, ClienteRemotoDto cliente, boolean usarPut) {
        validarCamposDeEscritura(curp, cliente);
        String segundoApellido = cliente.getSegundoApellido();
        LocalDate fechaLocal = cliente.obtenerFechaNacimientoLocal();
        String fechaRuta = FechasSistemaDueno.formatearParaRuta(fechaLocal);

        ClienteRemotoDto respuesta = TraductorExcepcionesSistemaDueno.ejecutar(
                () -> ejecutarPeticionEscritura(
                        curp, cliente, fechaRuta, segundoApellido, usarPut));
        if (esRespuestaVacia(respuesta)) {
            throw new SistemaDuenoRechazoException(
                    "El Sistema Dueno rechazo la escritura del cliente (200 vacio).");
        }
        FechasSistemaDueno.convertirALocalDate(respuesta.getFechaNacimiento());
        return respuesta;
    }

    private void validarCamposDeEscritura(String curp, ClienteRemotoDto cliente) {
        validarSegmentoObligatorio(curp, "La CURP");
        validarSegmentoObligatorio(cliente.getDireccion(), "La direccion");
        validarSegmentoObligatorio(cliente.getNombres(), "El nombre");
        validarSegmentoObligatorio(cliente.getPrimerApellido(), "El primer apellido");
        if (cliente.getSegundoApellido() != null) {
            validarSinDiagonales(cliente.getSegundoApellido(), "El segundo apellido");
        }
    }

    private ClienteRemotoDto ejecutarPeticionEscritura(
            String curp,
            ClienteRemotoDto cliente,
            String fechaRuta,
            String segundoApellido,
            boolean usarPut) {
        if (segundoApellido == null) {
            return enviarSinSegundoApellido(curp, cliente, fechaRuta);
        }
        return enviarConSegundoApellido(
                curp, cliente, fechaRuta, segundoApellido, usarPut);
    }

    private ClienteRemotoDto enviarConSegundoApellido(
            String curp,
            ClienteRemotoDto cliente,
            String fechaRuta,
            String segundoApellido,
            boolean usarPut) {
        RestClient.RequestBodyUriSpec peticion
                = usarPut ? restClient.put() : restClient.post();
        return peticion
                .uri(
                        RUTA_CON_SEGUNDO_APELLIDO,
                        curp,
                        cliente.getDireccion(),
                        fechaRuta,
                        cliente.getNombres(),
                        cliente.getPrimerApellido(),
                        segundoApellido)
                .retrieve()
                .body(ClienteRemotoDto.class);
    }

    private ClienteRemotoDto enviarSinSegundoApellido(
            String curp, ClienteRemotoDto cliente, String fechaRuta) {
        return restClient.post()
                .uri(
                        RUTA_SIN_SEGUNDO_APELLIDO,
                        curp,
                        cliente.getDireccion(),
                        fechaRuta,
                        cliente.getNombres(),
                        cliente.getPrimerApellido())
                .retrieve()
                .body(ClienteRemotoDto.class);
    }

    private boolean esRespuestaVacia(ClienteRemotoDto respuesta) {
        return respuesta == null
                || respuesta.getCurp() == null
                || respuesta.getCurp().trim().isEmpty();
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
            agregarSiEsValido(clientes, dto);
        }
        return List.copyOf(clientes);
    }

    private void agregarSiEsValido(List<Cliente> clientes, ClienteRemotoDto dto) {
        if (dto == null || esRespuestaVacia(dto)) {
            return;
        }
        try {
            clientes.add(convertirADominio(dto));
        } catch (RuntimeException ex) {
            LOG.warn("Se omite un cliente invalido del Sistema Dueno: {}",
                    ex.getMessage());
        }
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
