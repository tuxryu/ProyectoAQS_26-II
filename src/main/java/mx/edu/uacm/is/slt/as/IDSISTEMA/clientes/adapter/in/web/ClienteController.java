package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.in.web;

import java.util.List;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.in.web.dto.ClienteRespuestaDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultaClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.FiltroCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Adaptador REST de entrada para las consultas GET de clientes (lectura por
 * CURP y listados con filtros y paginación).
 */
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ConsultaClienteUseCase consultaClienteUseCase;

    public ClienteController(ConsultaClienteUseCase consultaClienteUseCase) {
        this.consultaClienteUseCase = Objects.requireNonNull(
                consultaClienteUseCase, "El caso de uso ConsultaClienteUseCase es obligatorio.");
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
     * Lista clientes con filtros opcionales y paginación en memoria.
     *
     * @param nombres nombres del cliente (opcional)
     * @param primerApellido primer apellido (opcional)
     * @param segundoApellido segundo apellido (opcional)
     * @param pagina número de página (por defecto 0)
     * @param tamano tamaño de página (por defecto 20)
     * @return página de clientes
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