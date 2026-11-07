package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.in.web.dto;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.in.web.dto.BeneficiarioRespuestaDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.in.web.dto.BeneficiarioSolicitudDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.AltaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ConsultaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
/**
 * Adaptador REST de entrada para el modulo de beneficiarios: alta vinculada a
 * una poliza (POST /polizas/{clave}/beneficiarios) y consultas GET (por llave
 * compuesta o listado completo de la poliza).
 */
@RestController
@RequestMapping("/polizas/{clave}/beneficiarios")

public class BeneficiarioController {
    private final AltaBeneficiarioUseCase altaBeneficiarioUseCase;
    private final ConsultaBeneficiarioUseCase consultaBeneficiarioUseCase;

    public BeneficiarioController(
            AltaBeneficiarioUseCase altaBeneficiarioUseCase,
            ConsultaBeneficiarioUseCase consultaBeneficiarioUseCase) {
        this.altaBeneficiarioUseCase = Objects.requireNonNull(
                altaBeneficiarioUseCase,
                "El caso de uso AltaBeneficiarioUseCase es obligatorio.");
        this.consultaBeneficiarioUseCase = Objects.requireNonNull(
                consultaBeneficiarioUseCase,
                "El caso de uso ConsultaBeneficiarioUseCase es obligatorio.");
    }
    /**
     * Registra un beneficiario vinculado a la poliza indicada
     * (POST /polizas/{clave}/beneficiarios).
     *
     * @param clave identificador UUID de la poliza
     * @param solicitud datos del beneficiario a registrar
     * @return beneficiario registrado con HTTP 201 Created
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BeneficiarioRespuestaDto registrar(
            @PathVariable UUID clave,
            @RequestBody BeneficiarioSolicitudDto solicitud) {
        if (solicitud == null || solicitud.porcentaje() == null) {
            throw new SolicitudInvalidaException(
                    "Los datos del beneficiario y su porcentaje son obligatorios.");
        }
        String segundoApellido = normalizarSegundoApellido(solicitud.segundoApellido());
        Beneficiario nuevo;
        try {
            nuevo = new Beneficiario(
                    solicitud.nombres(),
                    solicitud.primerApellido(),
                    segundoApellido,
                    solicitud.fechaNacimiento(),
                    clave,
                    solicitud.porcentaje());
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException(ex.getMessage());
        }
        Beneficiario registrado = altaBeneficiarioUseCase.registrar(nuevo);
        return BeneficiarioRespuestaDto.desde(registrado);
    }
    /**
     * Consulta los beneficiarios de una poliza (GET /polizas/{clave}/beneficiarios).
     * Si no se envian parametros de filtro, devuelve la lista de beneficiarios
     * vinculados a la poliza; si se envian nombres, primerApellido y
     * fechaNacimiento, devuelve el beneficiario individual.
     *
     * @param clave identificador UUID de la poliza
     * @param nombres nombres del beneficiario (opcional para listado)
     * @param primerApellido primer apellido del beneficiario (opcional para listado)
     * @param segundoApellido segundo apellido del beneficiario (opcional)
     * @param fechaNacimiento fecha de nacimiento yyyy-MM-dd (opcional para listado)
     * @return respuesta HTTP 200 con la lista o el beneficiario individual
     */
    @GetMapping
    public ResponseEntity<?> consultar(
            @PathVariable UUID clave,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaNacimiento) {
        boolean sinFiltros = nombres == null
                && primerApellido == null
                && segundoApellido == null
                && fechaNacimiento == null;
        if (sinFiltros) {
            List<BeneficiarioRespuestaDto> lista = consultaBeneficiarioUseCase
                    .listarPorPoliza(clave)
                    .stream()
                    .map(BeneficiarioRespuestaDto::desde)
                    .toList();
            return ResponseEntity.ok(lista);
        }
        if (nombres == null || primerApellido == null || fechaNacimiento == null) {
            throw new SolicitudInvalidaException(
                    "Para consultar un beneficiario individual se requieren "
                            + "nombres, primerApellido y fechaNacimiento.");
        }
        Beneficiario encontrado = consultaBeneficiarioUseCase.consultarPorDatos(
                clave,
                nombres,
                primerApellido,
                normalizarSegundoApellido(segundoApellido),
                fechaNacimiento);
        return ResponseEntity.ok(BeneficiarioRespuestaDto.desde(encontrado));
    }
    private String normalizarSegundoApellido(String segundoApellido) {
        if (segundoApellido == null || segundoApellido.trim().isEmpty()) {
            return null;
        }
        return segundoApellido;
    }
}
