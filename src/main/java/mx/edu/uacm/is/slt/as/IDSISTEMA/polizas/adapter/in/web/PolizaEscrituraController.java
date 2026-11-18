package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.in.web;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.in.web.dto.PolizaRespuestaDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.in.web.dto.PolizaSolicitudDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CancelarPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CrearPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ModificacionPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;

/**
 * Adaptador REST de entrada para las operaciones de escritura de polizas.
 * Soporta tanto las rutas REST con JSON de la ERS (/polizas) como las rutas
 * por segmentos (/poliza/...):
 *
 * <ul>
 *   <li>POST   /polizas (JSON, genera UUID propio y devuelve 201 + Location)</li>
 *   <li>PATCH  /polizas/{clave} (JSON, modificacion parcial conservando nulls)</li>
 *   <li>DELETE /polizas/{clave}?confirmar=true (exige confirmar=true -> 400)</li>
 *   <li>POST   /poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}</li>
 *   <li>PUT    /poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}</li>
 *   <li>DELETE /poliza/{clave}</li>
 * </ul>
 */
@RestController
public class PolizaEscrituraController {

    private final CrearPolizaUseCasePort crearPolizaUseCase;

    private final ModificacionPolizaUseCasePort modificacionPolizaUseCase;

    private final CancelarPolizaUseCasePort cancelarPolizaUseCase;

    private final ConsultaPolizaUseCase consultaPolizaUseCase;

    public PolizaEscrituraController(
            CrearPolizaUseCasePort crearPolizaUseCase,
            ModificacionPolizaUseCasePort modificacionPolizaUseCase,
            CancelarPolizaUseCasePort cancelarPolizaUseCase) {
        this(
                crearPolizaUseCase,
                modificacionPolizaUseCase,
                cancelarPolizaUseCase,
                Optional.empty());
    }

    @Autowired
    public PolizaEscrituraController(
            CrearPolizaUseCasePort crearPolizaUseCase,
            ModificacionPolizaUseCasePort modificacionPolizaUseCase,
            CancelarPolizaUseCasePort cancelarPolizaUseCase,
            Optional<ConsultaPolizaUseCase> consultaPolizaUseCase) {
        this.crearPolizaUseCase = Objects.requireNonNull(
                crearPolizaUseCase, "El caso de uso CrearPolizaUseCasePort es obligatorio.");
        this.modificacionPolizaUseCase = Objects.requireNonNull(
                modificacionPolizaUseCase,
                "El caso de uso ModificacionPolizaUseCasePort es obligatorio.");
        this.cancelarPolizaUseCase = Objects.requireNonNull(
                cancelarPolizaUseCase, "El caso de uso CancelarPolizaUseCasePort es obligatorio.");
        this.consultaPolizaUseCase = Objects.requireNonNull(
                consultaPolizaUseCase,
                "El contenedor Optional de ConsultaPolizaUseCase no puede ser nulo.")
                .orElse(null);
    }

    /**
     * Registra una nueva poliza generando UUID propio (POST /polizas -> 201 + Location).
     */
    @PostMapping("/polizas")
    public ResponseEntity<PolizaRespuestaDto> crearConJson(
            @RequestBody PolizaSolicitudDto solicitud) {
        if (solicitud == null
                || solicitud.tipo() == null
                || solicitud.monto() == null
                || solicitud.curpCliente() == null) {
            throw new SolicitudInvalidaException(
                    "Los campos tipo, monto y curpCliente son obligatorios.");
        }
        Poliza nueva = construirPolizaValidada(
                UUID.randomUUID(),
                solicitud.tipo(),
                solicitud.monto(),
                solicitud.descripcion(),
                solicitud.curpCliente());
        Poliza creada = crearPolizaUseCase.crearPoliza(nueva);
        URI ubicacion = URI.create("/polizas/" + creada.getClave());
        return ResponseEntity.created(ubicacion).body(PolizaRespuestaDto.desde(creada));
    }

    /**
     * Modifica parcialmente una poliza existente conservando los campos que
     * vengan en null (PATCH /polizas/{clave} -> 200).
     */
    @PatchMapping("/polizas/{clave}")
    public PolizaRespuestaDto modificarParcial(
            @PathVariable UUID clave,
            @RequestBody PolizaSolicitudDto solicitud) {
        if (solicitud == null) {
            throw new SolicitudInvalidaException("El cuerpo de la solicitud es obligatorio.");
        }
        if (consultaPolizaUseCase == null) {
            throw new IllegalStateException(
                    "El caso de uso ConsultaPolizaUseCase no esta disponible.");
        }
        Poliza actual = consultaPolizaUseCase.consultarPorClave(clave);
        int tipoFinal = solicitud.tipo() != null ? solicitud.tipo() : (int) actual.getTipo();
        float montoFinal = solicitud.monto() != null ? solicitud.monto() : actual.getMonto();
        String descripcionFinal = solicitud.descripcion() != null
                ? solicitud.descripcion()
                : actual.getDescripcion();
        String curpFinal = solicitud.curpCliente() != null
                ? solicitud.curpCliente()
                : actual.getCurpCliente();

        Poliza fusionada = construirPolizaValidada(
                clave, tipoFinal, montoFinal, descripcionFinal, curpFinal);
        Poliza actualizada = modificacionPolizaUseCase.modificarPoliza(clave, fusionada);
        return PolizaRespuestaDto.desde(actualizada);
    }

    /**
     * Da de baja una poliza exigiendo confirmar=true
     * (DELETE /polizas/{clave}?confirmar=true -> 204).
     */
    @DeleteMapping("/polizas/{clave}")
    public ResponseEntity<Void> eliminarConConfirmacion(
            @PathVariable UUID clave,
            @RequestParam(defaultValue = "false") boolean confirmar) {
        if (!confirmar) {
            throw new SolicitudInvalidaException(
                    "Debe enviar el parametro confirmar=true para eliminar la poliza.");
        }
        cancelarPolizaUseCase.cancelarPoliza(clave);
        return ResponseEntity.noContent().build();
    }

    /**
     * Registra una nueva poliza por ruta
     * (POST /poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}).
     */
    @PostMapping("/poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}")
    public ResponseEntity<PolizaRespuestaDto> registrar(
            @PathVariable UUID clave,
            @PathVariable byte tipo,
            @PathVariable float monto,
            @PathVariable String descripcion,
            @PathVariable String curpCliente) {
        Poliza poliza = construirPolizaValidada(
                clave, (int) tipo, monto, descripcion, curpCliente);
        Poliza creada = crearPolizaUseCase.crearPoliza(poliza);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PolizaRespuestaDto.desde(creada));
    }

    /**
     * Actualiza una poliza existente por ruta
     * (PUT /poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}).
     */
    @PutMapping("/poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}")
    public PolizaRespuestaDto actualizar(
            @PathVariable UUID clave,
            @PathVariable byte tipo,
            @PathVariable float monto,
            @PathVariable String descripcion,
            @PathVariable String curpCliente) {
        Poliza poliza = construirPolizaValidada(
                clave, (int) tipo, monto, descripcion, curpCliente);
        Poliza actualizada = modificacionPolizaUseCase.modificarPoliza(clave, poliza);
        return PolizaRespuestaDto.desde(actualizada);
    }

    /**
     * Elimina una poliza por su clave (DELETE /poliza/{clave}).
     */
    @DeleteMapping("/poliza/{clave}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID clave) {
        cancelarPolizaUseCase.cancelarPoliza(clave);
        return ResponseEntity.noContent().build();
    }

    private Poliza construirPolizaValidada(
            UUID clave,
            int tipo,
            float monto,
            String descripcion,
            String curpCliente) {
        if (tipo < 0 || tipo > 2) {
            throw new ReglaNegocioException("El tipo de poliza debe estar entre 0 y 2.");
        }
        try {
            return new Poliza(clave, (byte) tipo, monto, descripcion, curpCliente);
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException(ex.getMessage());
        }
    }
}
