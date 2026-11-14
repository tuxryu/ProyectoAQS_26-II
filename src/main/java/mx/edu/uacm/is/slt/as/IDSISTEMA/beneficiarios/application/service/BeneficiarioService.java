package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ConsultarBeneficiarioUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.RegistrarBeneficiarioUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out.BeneficiarioSistemaDuenoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out.PolizaServicioPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;

/**
 * Servicio de aplicacion para el modulo de beneficiarios: implementa la
 * consulta GET (individual y listado por poliza) y el alta vinculada validando
 * existencia de poliza (404), duplicidad (409), suma de porcentajes menor o
 * igual a 100% (422) y relectura posterior a la escritura.
 */
@Service
public class BeneficiarioService
        implements RegistrarBeneficiarioUseCasePort, ConsultarBeneficiarioUseCasePort {

    private static final int PORCENTAJE_MAXIMO_POLIZA = 100;

    private final BeneficiarioSistemaDuenoPort beneficiarioSistemaDuenoPort;
    private final PolizaServicioPort polizaServicioPort;

    /**
     * Crea el servicio con sus puertos de salida.
     *
     * @param beneficiarioSistemaDuenoPort puerto hacia el Sistema Dueno
     * @param polizaServicioPort puerto inter-modulo para verificar la poliza
     */
    public BeneficiarioService(
            BeneficiarioSistemaDuenoPort beneficiarioSistemaDuenoPort,
            PolizaServicioPort polizaServicioPort) {
        this.beneficiarioSistemaDuenoPort = Objects.requireNonNull(
                beneficiarioSistemaDuenoPort,
                "El puerto BeneficiarioSistemaDuenoPort es obligatorio.");
        this.polizaServicioPort = Objects.requireNonNull(
                polizaServicioPort,
                "El puerto PolizaServicioPort es obligatorio.");
    }

    /**
     * Constructor para inyeccion de Spring cuando el adaptador inter-modulo de
     * polizas es opcional en el contexto.
     *
     * @param beneficiarioSistemaDuenoPort puerto hacia el Sistema Dueno
     * @param polizaServicioPort Optional con el puerto inter-modulo de polizas
     */
    @Autowired
    public BeneficiarioService(
            BeneficiarioSistemaDuenoPort beneficiarioSistemaDuenoPort,
            Optional<PolizaServicioPort> polizaServicioPort) {
        this.beneficiarioSistemaDuenoPort = Objects.requireNonNull(
                beneficiarioSistemaDuenoPort,
                "El puerto BeneficiarioSistemaDuenoPort es obligatorio.");
        this.polizaServicioPort = Objects.requireNonNull(
                polizaServicioPort,
                "El contenedor Optional de PolizaServicioPort no puede ser nulo.")
                .orElse(clave -> true);
    }

    @Override
    public Beneficiario registrarBeneficiario(Beneficiario beneficiario) {
        return registrar(beneficiario);
    }

    /**
     * Registra un nuevo beneficiario vinculado a una poliza existente,
     * verificando que no este duplicado y que la suma de porcentajes de la
     * poliza no rebase el 100%.
     *
     * @param beneficiario entidad de dominio a registrar
     * @return beneficiario registrado y verificado
     */
    public Beneficiario registrar(Beneficiario beneficiario) {
        if (beneficiario == null || beneficiario.getId() == null) {
            throw new SolicitudInvalidaException(
                    "Los datos del beneficiario son obligatorios.");
        }
        UUID clavePoliza = beneficiario.getClavePoliza();
        exigirPolizaExistente(clavePoliza);

        Optional<Beneficiario> existente = beneficiarioSistemaDuenoPort.buscarPorDatos(
                beneficiario.getFechaNacimiento(),
                clavePoliza,
                beneficiario.getNombres(),
                beneficiario.getPrimerApellido(),
                beneficiario.getSegundoApellido());
        if (existente.isPresent()) {
            throw new ConflictoDatosException(
                    "El beneficiario ya se encuentra registrado en la poliza "
                    + clavePoliza + ".");
        }

        List<Beneficiario> actuales = beneficiarioSistemaDuenoPort.listarPorPoliza(clavePoliza);
        int sumaActual = actuales.stream()
                .mapToInt(Beneficiario::getPorcentaje)
                .sum();
        if (sumaActual + beneficiario.getPorcentaje() > PORCENTAJE_MAXIMO_POLIZA) {
            throw new ReglaNegocioException(
                    "La suma de porcentajes de los beneficiarios para la poliza "
                    + clavePoliza + " excede el 100% (actual: "
                    + sumaActual + "%, solicitado: "
                    + beneficiario.getPorcentaje() + "%).");
        }

        beneficiarioSistemaDuenoPort.guardar(beneficiario);

        Beneficiario releido = beneficiarioSistemaDuenoPort.buscarPorDatos(
                beneficiario.getFechaNacimiento(),
                clavePoliza,
                beneficiario.getNombres(),
                beneficiario.getPrimerApellido(),
                beneficiario.getSegundoApellido())
                .orElseThrow(() -> new SistemaDuenoRechazoException(
                        "El Sistema Dueno no persistio el beneficiario tras la escritura."));

        if (!releido.equals(beneficiario)) {
            throw new SistemaDuenoRechazoException(
                    "Los datos releidos del beneficiario no coinciden con los enviados.");
        }
        return releido;
    }

    /**
     * Consulta un beneficiario especifico por su llave compuesta dentro de una
     * poliza.
     *
     * @param clavePoliza clave UUID de la poliza
     * @param nombres nombres del beneficiario
     * @param primerApellido primer apellido del beneficiario
     * @param segundoApellido segundo apellido del beneficiario (opcional)
     * @param fechaNacimiento fecha de nacimiento del beneficiario
     * @return el beneficiario encontrado
     */
    public Beneficiario consultarPorDatos(
            UUID clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellido,
            LocalDate fechaNacimiento) {
        if (clavePoliza == null) {
            throw new SolicitudInvalidaException("La clave de la poliza es obligatoria.");
        }
        validarTextoConsulta(nombres, "El nombre");
        validarTextoConsulta(primerApellido, "El primer apellido");
        if (segundoApellido != null && !segundoApellido.trim().isEmpty()) {
            validarSinDiagonales(segundoApellido, "El segundo apellido");
        }
        if (fechaNacimiento == null) {
            throw new SolicitudInvalidaException("La fecha de nacimiento es obligatoria.");
        }
        String segundoNormalizado
                = (segundoApellido != null && segundoApellido.trim().isEmpty())
                        ? null
                        : segundoApellido;

        return beneficiarioSistemaDuenoPort.buscarPorDatos(
                fechaNacimiento,
                clavePoliza,
                nombres,
                primerApellido,
                segundoNormalizado)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontro el beneficiario solicitado en la poliza "
                        + clavePoliza + "."));
    }

    @Override
    public List<Beneficiario> consultarPorPoliza(String polizaId) {
        if (polizaId == null || polizaId.trim().isEmpty()) {
            throw new SolicitudInvalidaException("La clave de la poliza es obligatoria.");
        }
        final UUID clavePoliza;
        try {
            clavePoliza = UUID.fromString(polizaId.trim());
        } catch (IllegalArgumentException ex) {
            throw new SolicitudInvalidaException(
                    "La clave de la poliza no tiene un formato UUID valido.");
        }
        return listarPorPoliza(clavePoliza);
    }

    /**
     * Lista todos los beneficiarios asociados a una poliza.
     *
     * @param clavePoliza clave UUID de la poliza
     * @return lista de beneficiarios de la poliza
     */
    public List<Beneficiario> listarPorPoliza(UUID clavePoliza) {
        if (clavePoliza == null) {
            throw new SolicitudInvalidaException("La clave de la poliza es obligatoria.");
        }
        exigirPolizaExistente(clavePoliza);
        return beneficiarioSistemaDuenoPort.listarPorPoliza(clavePoliza);
    }

    private void exigirPolizaExistente(UUID clavePoliza) {
        if (!polizaServicioPort.existePolizaPorClave(clavePoliza)) {
            throw new RecursoNoEncontradoException(
                    "No existe la poliza con clave " + clavePoliza + ".");
        }
    }

    private void validarTextoConsulta(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new SolicitudInvalidaException(campo + " es obligatorio.");
        }
        validarSinDiagonales(valor, campo);
    }

    private void validarSinDiagonales(String valor, String campo) {
        if (valor.contains("/") || valor.contains("\\")) {
            throw new ReglaNegocioException(campo + " no puede contener diagonales.");
        }
    }
}
