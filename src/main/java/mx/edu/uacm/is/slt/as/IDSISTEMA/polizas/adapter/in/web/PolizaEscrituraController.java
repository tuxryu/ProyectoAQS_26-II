package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.in.web;

import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.in.web.dto.PolizaRespuestaDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CancelarPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CrearPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ModificacionPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;

/**
 * Adaptador REST de entrada para las operaciones de escritura de polizas.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST   /poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}</li>
 *   <li>PUT    /poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}</li>
 *   <li>DELETE /poliza/{clave}</li>
 * </ul>
 */
@RestController
@RequestMapping("/poliza")
public class PolizaEscrituraController {

    private final CrearPolizaUseCasePort crearPolizaUseCase;
    private final ModificacionPolizaUseCasePort modificacionPolizaUseCase;
    private final CancelarPolizaUseCasePort cancelarPolizaUseCase;

    public PolizaEscrituraController(
            CrearPolizaUseCasePort crearPolizaUseCase,
            ModificacionPolizaUseCasePort modificacionPolizaUseCase,
            CancelarPolizaUseCasePort cancelarPolizaUseCase) {
        this.crearPolizaUseCase = Objects.requireNonNull(
                crearPolizaUseCase, "El caso de uso CrearPolizaUseCasePort es obligatorio.");
        this.modificacionPolizaUseCase = Objects.requireNonNull(
                modificacionPolizaUseCase,
                "El caso de uso ModificacionPolizaUseCasePort es obligatorio.");
        this.cancelarPolizaUseCase = Objects.requireNonNull(
                cancelarPolizaUseCase, "El caso de uso CancelarPolizaUseCasePort es obligatorio.");
    }

    /**
     * Registra una nueva poliza
     * (POST /poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}).
     */
    @PostMapping("/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}")
    public ResponseEntity<PolizaRespuestaDto> registrar(
            @PathVariable UUID clave,
            @PathVariable byte tipo,
            @PathVariable float monto,
            @PathVariable String descripcion,
            @PathVariable String curpCliente) {
        Poliza poliza = new Poliza(clave, tipo, monto, descripcion, curpCliente);
        Poliza creada = crearPolizaUseCase.crearPoliza(poliza);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PolizaRespuestaDto.desde(creada));
    }

    /**
     * Actualiza una poliza existente
     * (PUT /poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}).
     */
    @PutMapping("/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}")
    public PolizaRespuestaDto actualizar(
            @PathVariable UUID clave,
            @PathVariable byte tipo,
            @PathVariable float monto,
            @PathVariable String descripcion,
            @PathVariable String curpCliente) {
        Poliza poliza = new Poliza(clave, tipo, monto, descripcion, curpCliente);
        Poliza actualizada = modificacionPolizaUseCase.modificarPoliza(clave, poliza);
        return PolizaRespuestaDto.desde(actualizada);
    }

    /**
     * Elimina una poliza por su clave (DELETE /poliza/{clave}).
     */
    @DeleteMapping("/{clave}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID clave) {
        cancelarPolizaUseCase.cancelarPoliza(clave);
        return ResponseEntity.noContent().build();
    }
}