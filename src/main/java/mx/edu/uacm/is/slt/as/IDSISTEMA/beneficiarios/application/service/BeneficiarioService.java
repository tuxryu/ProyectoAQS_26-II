package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.AltaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.BajaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ConsultaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ConsultarBeneficiarioUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ModificacionBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.RegistrarBeneficiarioUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out.BeneficiarioSistemaDuenoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out.PolizaServicioPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.exception.BeneficiarioNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;

/**
 * Servicio de aplicacion para el modulo de beneficiarios: implementa consulta
 * GET (general, individual y por poliza), alta vinculada y las operaciones
 * compuestas de baja y modificacion individual (respaldo en memoria, baja en
 * cascada de la poliza, recreacion con la misma clave UUID y restauracion
 * verificada).
 */
@Service
public class BeneficiarioService
        implements AltaBeneficiarioUseCase,
        BajaBeneficiarioUseCase,
        ConsultaBeneficiarioUseCase,
        ModificacionBeneficiarioUseCase,
        RegistrarBeneficiarioUseCasePort,
        ConsultarBeneficiarioUseCasePort {

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

    @Override
    public Beneficiario registrar(Beneficiario beneficiario) {
        if (beneficiario == null || beneficiario.getId() == null) {
            throw new SolicitudInvalidaException(
                    "Los datos del beneficiario son obligatorios.");
        }
        Beneficiario.validarFechaNacimientoParaRegistro(beneficiario.getFechaNacimiento());
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
            int disponible = Math.max(0, PORCENTAJE_MAXIMO_POLIZA - sumaActual);
            throw new ReglaNegocioException(
                    "No es posible agregar el beneficiario porque la suma de porcentajes "
                    + "de la poliza " + clavePoliza + " excede el 100% (asignado actual: "
                    + sumaActual + "%, solicitado: " + beneficiario.getPorcentaje()
                    + "%, disponible: " + disponible + "%).");
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

    @Override
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
    public List<Beneficiario> listarTodos() {
        return beneficiarioSistemaDuenoPort.listarTodos();
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

    @Override
    public List<Beneficiario> listarPorPoliza(UUID clavePoliza) {
        if (clavePoliza == null) {
            throw new SolicitudInvalidaException("La clave de la poliza es obligatoria.");
        }
        exigirPolizaExistente(clavePoliza);
        return beneficiarioSistemaDuenoPort.listarPorPoliza(clavePoliza);
    }

    @Override
    public void eliminar(
            UUID clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellido,
            LocalDate fechaNacimiento) {
        validarLlaveCompuesta(
                clavePoliza, nombres, primerApellido, segundoApellido, fechaNacimiento);
        String segundoNorm = normalizarSegundoApellido(segundoApellido);
        exigirPolizaExistente(clavePoliza);

        List<Beneficiario> actuales = beneficiarioSistemaDuenoPort.listarPorPoliza(clavePoliza);
        List<Beneficiario> restantes = new ArrayList<>();
        boolean encontrado = false;
        for (Beneficiario actual : actuales) {
            if (!encontrado && coincideLlave(
                    actual, clavePoliza, nombres, primerApellido, segundoNorm, fechaNacimiento)) {
                encontrado = true;
            } else {
                restantes.add(actual);
            }
        }
        if (!encontrado) {
            throw new BeneficiarioNoEncontradoException(
                    "No se encontro el beneficiario solicitado en la poliza "
                    + clavePoliza + ".");
        }

        polizaServicioPort.reiniciarPolizaConservandoClave(clavePoliza);
        for (Beneficiario restante : restantes) {
            beneficiarioSistemaDuenoPort.guardar(restante);
        }

        Optional<Beneficiario> verificado = beneficiarioSistemaDuenoPort.buscarPorDatos(
                fechaNacimiento, clavePoliza, nombres, primerApellido, segundoNorm);
        if (verificado.isPresent()) {
            throw new SistemaDuenoRechazoException(
                    "El Sistema Dueno no elimino el beneficiario solicitado.");
        }
        int totalActual = beneficiarioSistemaDuenoPort.listarPorPoliza(clavePoliza).size();
        if (totalActual != restantes.size()) {
            throw new SistemaDuenoRechazoException(
                    "No se pudieron restaurar todos los beneficiarios restantes de la poliza.");
        }
    }

    @Override
    public Beneficiario actualizar(
            UUID clavePoliza,
            String nombresActuales,
            String primerApellidoActual,
            String segundoApellidoActual,
            LocalDate fechaNacimientoActual,
            Beneficiario beneficiarioActualizado) {
        validarLlaveCompuesta(
                clavePoliza,
                nombresActuales,
                primerApellidoActual,
                segundoApellidoActual,
                fechaNacimientoActual);
        if (beneficiarioActualizado == null || beneficiarioActualizado.getId() == null) {
            throw new SolicitudInvalidaException(
                    "Los datos actualizados del beneficiario son obligatorios.");
        }
        Beneficiario.validarFechaNacimientoParaRegistro(
                beneficiarioActualizado.getFechaNacimiento());
        if (!clavePoliza.equals(beneficiarioActualizado.getClavePoliza())) {
            throw new ReglaNegocioException(
                    "No se puede cambiar la clave de poliza al modificar un beneficiario.");
        }
        exigirPolizaExistente(clavePoliza);
        final String segundoActualNorm = normalizarSegundoApellido(segundoApellidoActual);

        List<Beneficiario> actuales = beneficiarioSistemaDuenoPort.listarPorPoliza(clavePoliza);
        Beneficiario original = null;
        List<Beneficiario> otros = new ArrayList<>();
        for (Beneficiario actual : actuales) {
            if (original == null && coincideLlave(
                    actual,
                    clavePoliza,
                    nombresActuales,
                    primerApellidoActual,
                    segundoActualNorm,
                    fechaNacimientoActual)) {
                original = actual;
            } else {
                otros.add(actual);
            }
        }
        if (original == null) {
            throw new BeneficiarioNoEncontradoException(
                    "No se encontro el beneficiario solicitado en la poliza "
                    + clavePoliza + ".");
        }

        String nuevoSegundoNorm = normalizarSegundoApellido(
                beneficiarioActualizado.getSegundoApellido());
        boolean existeDuplicado = otros.stream()
                .anyMatch(otro -> coincideLlave(
                        otro,
                        clavePoliza,
                        beneficiarioActualizado.getNombres(),
                        beneficiarioActualizado.getPrimerApellido(),
                        nuevoSegundoNorm,
                        beneficiarioActualizado.getFechaNacimiento()));
        if (existeDuplicado) {
            throw new ConflictoDatosException(
                    "Ya existe otro beneficiario con esos mismos datos en la poliza "
                    + clavePoliza + ".");
        }

        int sumaOtros = otros.stream()
                .mapToInt(Beneficiario::getPorcentaje)
                .sum();
        if (sumaOtros + beneficiarioActualizado.getPorcentaje() > PORCENTAJE_MAXIMO_POLIZA) {
            int maximoDisponible = Math.max(0, PORCENTAJE_MAXIMO_POLIZA - sumaOtros);
            throw new ReglaNegocioException(
                    "La suma de porcentajes de los beneficiarios para la poliza "
                    + clavePoliza + " excede el 100% (otros beneficiarios: "
                    + sumaOtros + "%, solicitado: " + beneficiarioActualizado.getPorcentaje()
                    + "%, maximo disponible: " + maximoDisponible + "%).");
        }

        boolean mismaLlave = coincideLlave(
                original,
                clavePoliza,
                beneficiarioActualizado.getNombres(),
                beneficiarioActualizado.getPrimerApellido(),
                nuevoSegundoNorm,
                beneficiarioActualizado.getFechaNacimiento());

        if (mismaLlave) {
            beneficiarioSistemaDuenoPort.guardar(beneficiarioActualizado);
        } else {
            polizaServicioPort.reiniciarPolizaConservandoClave(clavePoliza);
            for (Beneficiario otro : otros) {
                beneficiarioSistemaDuenoPort.guardar(otro);
            }
            beneficiarioSistemaDuenoPort.guardar(beneficiarioActualizado);
        }

        Beneficiario releido = beneficiarioSistemaDuenoPort.buscarPorDatos(
                beneficiarioActualizado.getFechaNacimiento(),
                clavePoliza,
                beneficiarioActualizado.getNombres(),
                beneficiarioActualizado.getPrimerApellido(),
                nuevoSegundoNorm)
                .orElseThrow(() -> new SistemaDuenoRechazoException(
                        "El Sistema Dueno no persistio los cambios del beneficiario."));

        if (!releido.equals(beneficiarioActualizado)) {
            throw new SistemaDuenoRechazoException(
                    "Los datos releidos del beneficiario no coinciden con los actualizados.");
        }
        return releido;
    }

    private void validarLlaveCompuesta(
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
    }

    private boolean coincideLlave(
            Beneficiario beneficiario,
            UUID clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellidoNormalizado,
            LocalDate fechaNacimiento) {
        return Objects.equals(beneficiario.getClavePoliza(), clavePoliza)
                && Objects.equals(beneficiario.getNombres(), nombres)
                && Objects.equals(beneficiario.getPrimerApellido(), primerApellido)
                && Objects.equals(
                        normalizarSegundoApellido(beneficiario.getSegundoApellido()),
                        segundoApellidoNormalizado)
                && Objects.equals(beneficiario.getFechaNacimiento(), fechaNacimiento);
    }

    private String normalizarSegundoApellido(String segundoApellido) {
        if (segundoApellido == null || segundoApellido.trim().isEmpty()) {
            return null;
        }
        return segundoApellido;
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
