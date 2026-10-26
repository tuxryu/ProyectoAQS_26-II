package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.in.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.in.web.dto.PolizaRespuestaDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.FiltroPoliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Adaptador REST de entrada para las consultas GET de polizas (lectura por
 * clave y listados con filtros y paginacion).
 */
@RestController
@RequestMapping("/polizas")
public class PolizaController {

    private final ConsultaPolizaUseCase consultaPolizaUseCase;

    public PolizaController(ConsultaPolizaUseCase consultaPolizaUseCase) {
        this.consultaPolizaUseCase = Objects.requireNonNull(
                consultaPolizaUseCase, "El caso de uso ConsultaPolizaUseCase es obligatorio.");
    }

    /**
     * Consulta una poliza por su clave UUID (GET /polizas/{clave}).
     *
     * @param clave identificador UUID de la poliza
     * @return datos de la poliza encontrada
     */
    @GetMapping("/{clave}")
    public PolizaRespuestaDto consultarPorClave(@PathVariable UUID clave) {
        Poliza poliza = consultaPolizaUseCase.consultarPorClave(clave);
        return PolizaRespuestaDto.desde(poliza);
    }

    /**
     * Lista polizas con a lo mucho un filtro opcional (curp, tipo o
     * fechaNacimientoBeneficiario) y paginacion en memoria.
     *
     * @param curp CURP del titular (opcional)
     * @param tipo tipo de poliza 0..2 (opcional)
     * @param fechaNacimientoBeneficiario fecha yyyy-MM-dd (opcional)
     * @param pagina numero de pagina (por defecto 0)
     * @param tamano tamano de pagina (por defecto 20)
     * @return pagina de polizas
     */
    @GetMapping
    public Pagina<PolizaRespuestaDto> listar(
            @RequestParam(required = false) String curp,
            @RequestParam(required = false) Integer tipo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaNacimientoBeneficiario,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        FiltroPoliza filtro = FiltroPoliza.desde(curp, tipo, fechaNacimientoBeneficiario);
        Pagina<Poliza> resultado = consultaPolizaUseCase.consultar(filtro, pagina, tamano);
        return mapearPagina(resultado);
    }

    /**
     * Busca polizas por nombre completo del cliente titular
     * (GET /polizas/busqueda/cliente).
     *
     * @param nombres nombres del cliente
     * @param primerApellido primer apellido del cliente
     * @param segundoApellido segundo apellido del cliente (opcional)
     * @param pagina numero de pagina (por defecto 0)
     * @param tamano tamano de pagina (por defecto 20)
     * @return pagina de polizas
     */
    @GetMapping("/busqueda/cliente")
    public Pagina<PolizaRespuestaDto> buscarPorCliente(
            @RequestParam String nombres,
            @RequestParam String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        FiltroPoliza filtro = new FiltroPoliza.PorCliente(
                nombres, primerApellido, segundoApellido);
        Pagina<Poliza> resultado = consultaPolizaUseCase.consultar(filtro, pagina, tamano);
        return mapearPagina(resultado);
    }

    /**
     * Busca polizas por nombre completo de un beneficiario
     * (GET /polizas/busqueda/beneficiario).
     *
     * @param nombres nombres del beneficiario
     * @param primerApellido primer apellido del beneficiario
     * @param segundoApellido segundo apellido del beneficiario (opcional)
     * @param pagina numero de pagina (por defecto 0)
     * @param tamano tamano de pagina (por defecto 20)
     * @return pagina de polizas
     */
    @GetMapping("/busqueda/beneficiario")
    public Pagina<PolizaRespuestaDto> buscarPorBeneficiario(
            @RequestParam String nombres,
            @RequestParam String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        FiltroPoliza filtro = new FiltroPoliza.PorBeneficiario(
                nombres, primerApellido, segundoApellido);
        Pagina<Poliza> resultado = consultaPolizaUseCase.consultar(filtro, pagina, tamano);
        return mapearPagina(resultado);
    }

    private Pagina<PolizaRespuestaDto> mapearPagina(Pagina<Poliza> pagina) {
        List<PolizaRespuestaDto> contenido = pagina.contenido().stream()
                .map(PolizaRespuestaDto::desde)
                .toList();
        return new Pagina<>(
                contenido,
                pagina.pagina(),
                pagina.tamano(),
                pagina.totalElementos(),
                pagina.totalPaginas());
    }
}
