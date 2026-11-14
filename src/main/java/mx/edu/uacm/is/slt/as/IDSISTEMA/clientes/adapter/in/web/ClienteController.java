package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.in.web;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.in.web.dto.ClienteRespuestaDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.in.web.dto.ClienteSolicitudDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ActualizarClienteUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultaClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.FiltroCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.RegistrarClienteUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Adaptador REST de entrada para las operaciones de lectura y escritura de
 * clientes (POST, PUT y consultas GET por CURP o nombre).
 */
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ConsultaClienteUseCase consultaClienteUseCase;

    private final RegistrarClienteUseCasePort registrarClienteUseCasePort;

    private final ActualizarClienteUseCasePort actualizarClienteUseCasePort;

    /**
     * Constructor para pruebas que inyectan unicamente el caso de uso de
     * consulta.
     *
     * @param consultaClienteUseCase caso de uso de consultas de clientes
     */
    public ClienteController(ConsultaClienteUseCase consultaClienteUseCase) {
        this(consultaClienteUseCase, Optional.empty(), Optional.empty());
    }

    /**
     * Constructor principal inyectado por Spring con los casos de uso de
     * consulta, registro y actualizacion de clientes.
     *
     * @param consultaClienteUseCase caso de uso de consultas
     * @param registrarClienteUseCasePort puerto de alta de cliente
     * @param actualizarClienteUseCasePort puerto de actualizacion de cliente
     */
    @Autowired
    public ClienteController(
            ConsultaClienteUseCase consultaClienteUseCase,
            Optional<RegistrarClienteUseCasePort> registrarClienteUseCasePort,
            Optional<ActualizarClienteUseCasePort> actualizarClienteUseCasePort) {
        this.consultaClienteUseCase = Objects.requireNonNull(
                consultaClienteUseCase,
                "El caso de uso ConsultaClienteUseCase es obligatorio.");
        this.registrarClienteUseCasePort = Objects.requireNonNull(
                registrarClienteUseCasePort,
                "El contenedor Optional de RegistrarClienteUseCasePort no puede ser nulo.")
                .orElse(null);
        this.actualizarClienteUseCasePort = Objects.requireNonNull(
                actualizarClienteUseCasePort,
                "El contenedor Optional de ActualizarClienteUseCasePort no puede ser nulo.")
                .orElse(null);
    }

    /**
     * Registra un nuevo cliente (POST /clientes -> 201 Created).
     *
     * @param solicitud datos del cliente a registrar
     * @return datos del cliente registrado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteRespuestaDto registrar(
            @RequestBody ClienteSolicitudDto solicitud) {
        if (solicitud == null) {
            throw new SolicitudInvalidaException(
                    "El cuerpo de la solicitud es obligatorio.");
        }
        Cliente nuevo = construirClienteValidado(solicitud.curp(), solicitud);
        Cliente creado = registrarClienteUseCasePort.registrarCliente(nuevo);
        return ClienteRespuestaDto.desde(creado);
    }

    /**
     * Actualiza los datos de un cliente existente (PUT /clientes/{curp} -> 200 OK).
     *
     * @param curp CURP del cliente en la ruta
     * @param solicitud datos actualizados del cliente
     * @return datos del cliente actualizado
     */
    @PutMapping("/{curp}")
    public ClienteRespuestaDto actualizar(
            @PathVariable String curp,
            @RequestBody ClienteSolicitudDto solicitud) {
        if (solicitud == null) {
            throw new SolicitudInvalidaException(
                    "El cuerpo de la solicitud es obligatorio.");
        }
        Cliente datosActualizados = construirClienteValidado(curp, solicitud);
        Cliente actualizado
                = actualizarClienteUseCasePort.actualizarCliente(datosActualizados);
        return ClienteRespuestaDto.desde(actualizado);
    }

    /**
     * Consulta un cliente por su CURP (GET /clientes/{curp}).
     *
     * @param curp CURP del cliente
     * @return datos del cliente encontrado
     */
    @GetMapping("/{curp}")
    public ClienteRespuestaDto consultarPorCurp(@PathVariable String curp) {
        Cliente cliente = consultaClienteUseCase.consultarPorCurp(curp);
        return ClienteRespuestaDto.desde(cliente);
    }

    /**
     * Lista clientes con filtros opcionales y paginacion en memoria.
     *
     * @param nombres nombres del cliente (opcional)
     * @param primerApellido primer apellido (opcional)
     * @param segundoApellido segundo apellido (opcional)
     * @param pagina numero de pagina (por defecto 0)
     * @param tamano tamano de pagina (por defecto 20)
     * @return pagina de clientes
     */
    @GetMapping
    public Pagina<ClienteRespuestaDto> listar(
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        FiltroCliente filtro = FiltroCliente.desde(nombres, primerApellido, segundoApellido);
        Pagina<Cliente> resultado = consultaClienteUseCase.consultar(filtro, pagina, tamano);
        return mapearPagina(resultado);
    }

    private Cliente construirClienteValidado(
            String curp, ClienteSolicitudDto solicitud) {
        String segundoApellido = (solicitud.segundoApellido() != null
                && solicitud.segundoApellido().trim().isEmpty())
                ? null
                : solicitud.segundoApellido();
        try {
            return new Cliente(
                    curp,
                    solicitud.nombres(),
                    solicitud.primerApellido(),
                    segundoApellido,
                    solicitud.direccion(),
                    solicitud.fechaNacimiento());
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException(ex.getMessage());
        }
    }

    private Pagina<ClienteRespuestaDto> mapearPagina(Pagina<Cliente> pagina) {
        List<ClienteRespuestaDto> contenido = pagina.contenido().stream()
                .map(ClienteRespuestaDto::desde)
                .toList();
        return new Pagina<>(
                contenido,
                pagina.pagina(),
                pagina.tamano(),
                pagina.totalElementos(),
                pagina.totalPaginas());
    }
}
