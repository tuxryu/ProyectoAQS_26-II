package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.in.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.in.web.dto.BeneficiarioRespuestaDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.in.web.dto.BeneficiarioSolicitudDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.BajaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ConsultarBeneficiarioUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ModificacionBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.RegistrarBeneficiarioUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.exception.BeneficiarioNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;

/**
 * Adaptador REST de entrada para el modulo de beneficiarios: listado general
 * (GET /beneficiarios), alta vinculada (POST /polizas/{clave}/beneficiarios),
 * consultas GET, modificacion individual (PUT) y baja individual (DELETE)
 * mediante operacion compuesta.
 */
@RestController
public class BeneficiarioController {

    private final RegistrarBeneficiarioUseCasePort registrarBeneficiarioUseCasePort;

    private final ConsultarBeneficiarioUseCasePort consultarBeneficiarioUseCasePort;

    private final ModificacionBeneficiarioUseCase modificacionBeneficiarioUseCase;

    private final BajaBeneficiarioUseCase bajaBeneficiarioUseCase;

    /**
     * Constructor compatible con pruebas que solo inyectan alta y consulta.
     *
     * @param registrarBeneficiarioUseCasePort puerto de alta de beneficiario
     * @param consultarBeneficiarioUseCasePort puerto de consulta de beneficiarios
     */
    public BeneficiarioController(
            Optional<RegistrarBeneficiarioUseCasePort> registrarBeneficiarioUseCasePort,
            Optional<ConsultarBeneficiarioUseCasePort> consultarBeneficiarioUseCasePort) {
        this(
                registrarBeneficiarioUseCasePort,
                consultarBeneficiarioUseCasePort,
                Optional.empty(),
                Optional.empty());
    }

    /**
     * Constructor principal inyectado por Spring con todos los puertos de
     * entrada de beneficiarios (alta, consulta, modificacion y baja).
     *
     * @param registrarBeneficiarioUseCasePort puerto de alta de beneficiario
     * @param consultarBeneficiarioUseCasePort puerto de consulta de beneficiarios
     * @param modificacionBeneficiarioUseCase puerto de modificacion compuesta
     * @param bajaBeneficiarioUseCase puerto de baja compuesta
     */
    @Autowired
    public BeneficiarioController(
            Optional<RegistrarBeneficiarioUseCasePort> registrarBeneficiarioUseCasePort,
            Optional<ConsultarBeneficiarioUseCasePort> consultarBeneficiarioUseCasePort,
            Optional<ModificacionBeneficiarioUseCase> modificacionBeneficiarioUseCase,
            Optional<BajaBeneficiarioUseCase> bajaBeneficiarioUseCase) {
        this.registrarBeneficiarioUseCasePort = Objects.requireNonNull(
                registrarBeneficiarioUseCasePort,
                "El contenedor Optional de RegistrarBeneficiarioUseCasePort no puede ser nulo.")
                .orElse(null);
        this.consultarBeneficiarioUseCasePort = Objects.requireNonNull(
                consultarBeneficiarioUseCasePort,
                "El contenedor Optional de ConsultarBeneficiarioUseCasePort no puede ser nulo.")
                .orElse(null);
        this.modificacionBeneficiarioUseCase = Objects.requireNonNull(
                modificacionBeneficiarioUseCase,
                "El contenedor Optional de ModificacionBeneficiarioUseCase no puede ser nulo.")
                .orElse(null);
        this.bajaBeneficiarioUseCase = Objects.requireNonNull(
                bajaBeneficiarioUseCase,
                "El contenedor Optional de BajaBeneficiarioUseCase no puede ser nulo.")
                .orElse(null);
    }

    /**
     * Lista todos los beneficiarios registrados en el sistema
     * (GET /beneficiarios -> 200 OK).
     *
     * @return lista completa de beneficiarios validos
     */
    @GetMapping("/beneficiarios")
    public List<BeneficiarioRespuestaDto> listarTodos() {
        return consultarBeneficiarioUseCasePort.listarTodos()
                .stream()
                .map(BeneficiarioRespuestaDto::desde)
                .toList();
    }

    /**
     * Registra un beneficiario vinculado a la poliza indicada
     * (POST /polizas/{clave}/beneficiarios -> 201 Created).
     *
     * @param clave identificador UUID de la poliza
     * @param solicitud datos del beneficiario a registrar
     * @return beneficiario registrado con HTTP 201 Created
     */
    @PostMapping("/polizas/{clave}/beneficiarios")
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
            Beneficiario.validarFechaNacimientoParaRegistro(solicitud.fechaNacimiento());
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
        Beneficiario registrado
                = registrarBeneficiarioUseCasePort.registrarBeneficiario(nuevo);
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
    @GetMapping("/polizas/{clave}/beneficiarios")
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
            List<BeneficiarioRespuestaDto> lista = consultarBeneficiarioUseCasePort
                    .consultarPorPoliza(clave.toString())
                    .stream()
                    .map(BeneficiarioRespuestaDto::desde)
                    .toList();
            return ResponseEntity.ok(lista);
        }
        if (nombres == null
                || nombres.trim().isEmpty()
                || primerApellido == null
                || primerApellido.trim().isEmpty()
                || fechaNacimiento == null) {
            throw new SolicitudInvalidaException(
                    "Para consultar un beneficiario individual se requieren "
                    + "nombres, primerApellido y fechaNacimiento.");
        }
        String segundoNormalizado = normalizarSegundoApellido(segundoApellido);
        Beneficiario encontrado = consultarBeneficiarioUseCasePort
                .consultarPorPoliza(clave.toString())
                .stream()
                .filter(item -> Objects.equals(item.getNombres(), nombres)
                        && Objects.equals(item.getPrimerApellido(), primerApellido)
                        && Objects.equals(
                                normalizarSegundoApellido(item.getSegundoApellido()),
                                segundoNormalizado)
                        && Objects.equals(item.getFechaNacimiento(), fechaNacimiento))
                .findFirst()
                .orElseThrow(() -> new BeneficiarioNoEncontradoException(
                        "No se encontro el beneficiario solicitado en la poliza "
                        + clave + "."));
        return ResponseEntity.ok(BeneficiarioRespuestaDto.desde(encontrado));
    }

    /**
     * Modifica los datos de un beneficiario individual de la poliza mediante
     * operacion compuesta (PUT /polizas/{clave}/beneficiarios -> 200 OK).
     *
     * @param clave identificador UUID de la poliza
     * @param nombres nombres actuales del beneficiario
     * @param primerApellido primer apellido actual del beneficiario
     * @param segundoApellido segundo apellido actual (opcional)
     * @param fechaNacimiento fecha de nacimiento actual (yyyy-MM-dd)
     * @param solicitud nuevos datos del beneficiario
     * @return beneficiario actualizado con HTTP 200 OK
     */
    @PutMapping("/polizas/{clave}/beneficiarios")
    public BeneficiarioRespuestaDto actualizar(
            @PathVariable UUID clave,
            @RequestParam String nombres,
            @RequestParam String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaNacimiento,
            @RequestBody BeneficiarioSolicitudDto solicitud) {
        if (solicitud == null || solicitud.porcentaje() == null) {
            throw new SolicitudInvalidaException(
                    "Los datos actualizados del beneficiario y su porcentaje son obligatorios.");
        }
        String nuevoSegundo = normalizarSegundoApellido(solicitud.segundoApellido());
        Beneficiario actualizado;
        try {
            Beneficiario.validarFechaNacimientoParaRegistro(solicitud.fechaNacimiento());
            actualizado = new Beneficiario(
                    solicitud.nombres(),
                    solicitud.primerApellido(),
                    nuevoSegundo,
                    solicitud.fechaNacimiento(),
                    clave,
                    solicitud.porcentaje());
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException(ex.getMessage());
        }
        Beneficiario resultado = modificacionBeneficiarioUseCase.actualizar(
                clave,
                nombres,
                primerApellido,
                normalizarSegundoApellido(segundoApellido),
                fechaNacimiento,
                actualizado);
        return BeneficiarioRespuestaDto.desde(resultado);
    }

    /**
     * Elimina un beneficiario individual conservando la poliza y el resto de
     * sus beneficiarios mediante operacion compuesta
     * (DELETE /polizas/{clave}/beneficiarios?confirmar=true -> 204 No Content).
     *
     * @param clave identificador UUID de la poliza
     * @param nombres nombres del beneficiario a eliminar
     * @param primerApellido primer apellido del beneficiario
     * @param segundoApellido segundo apellido (opcional)
     * @param fechaNacimiento fecha de nacimiento (yyyy-MM-dd)
     * @param confirmar confirmacion explicita obligatoria ({@code true})
     */
    @DeleteMapping("/polizas/{clave}/beneficiarios")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable UUID clave,
            @RequestParam String nombres,
            @RequestParam String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaNacimiento,
            @RequestParam(defaultValue = "false") boolean confirmar) {
        if (!confirmar) {
            throw new SolicitudInvalidaException(
                    "Debe confirmar explicitamente la baja del beneficiario (?confirmar=true).");
        }
        bajaBeneficiarioUseCase.eliminar(
                clave,
                nombres,
                primerApellido,
                normalizarSegundoApellido(segundoApellido),
                fechaNacimiento);
    }

    private String normalizarSegundoApellido(String segundoApellido) {
        if (segundoApellido == null || segundoApellido.trim().isEmpty()) {
            return null;
        }
        return segundoApellido;
    }
}
