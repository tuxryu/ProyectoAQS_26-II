package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.adapter.in.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.FuncionalidadNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;

/**
 * Manejador global de excepciones para traducir errores de dominio e
 * infraestructura a los codigos HTTP de la especificacion (400, 404, 422, 501,
 * 502 y 503).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Traduce solicitudes mal formadas o con mas de un filtro a HTTP 400.
     *
     * @param ex excepcion de solicitud invalida
     * @return respuesta 400 Bad Request
     */
    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<Map<String, Object>> manejarSolicitudInvalida(
            SolicitudInvalidaException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Traduce la falta de un parametro requerido o un UUID/fecha ilegible en
     * ruta o query a HTTP 400.
     *
     * @param ex excepcion de parametro ausente o tipo incompatible
     * @return respuesta 400 Bad Request
     */
    @ExceptionHandler({
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String, Object>> manejarParametroInvalido(Exception ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Traduce recursos no encontrados a HTTP 404.
     *
     * @param ex excepcion de recurso no encontrado
     * @return respuesta 404 Not Found
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(
            RecursoNoEncontradoException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Traduce violaciones de reglas de dominio (tipo fuera de 0..2, diagonal en
     * ruta, etc.) a HTTP 422.
     *
     * @param ex excepcion de regla de negocio
     * @return respuesta 422 Unprocessable Entity
     */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> manejarReglaNegocio(
            ReglaNegocioException ex) {
        return construirRespuesta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /**
     * Traduce funcionalidades pendientes de implementar a HTTP 501.
     *
     * @param ex excepcion de funcionalidad no disponible
     * @return respuesta 501 Not Implemented
     */
    @ExceptionHandler(FuncionalidadNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> manejarNoImplementado(
            FuncionalidadNoDisponibleException ex) {
        return construirRespuesta(HttpStatus.NOT_IMPLEMENTED, ex.getMessage());
    }

    /**
     * Traduce rechazos o datos inconsistentes del Sistema Dueno a HTTP 502.
     *
     * @param ex excepcion de rechazo del Sistema Dueno
     * @return respuesta 502 Bad Gateway
     */
    @ExceptionHandler(SistemaDuenoRechazoException.class)
    public ResponseEntity<Map<String, Object>> manejarRechazoRemoto(
            SistemaDuenoRechazoException ex) {
        return construirRespuesta(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    /**
     * Traduce indisponibilidad o timeouts del Sistema Dueno a HTTP 503.
     *
     * @param ex excepcion de indisponibilidad del Sistema Dueno
     * @return respuesta 503 Service Unavailable
     */
    @ExceptionHandler(SistemaDuenoNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> manejarNoDisponibleRemoto(
            SistemaDuenoNoDisponibleException ex) {
        return construirRespuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> construirRespuesta(
            HttpStatus estado, String mensaje) {
        Map<String, Object> cuerpo = Map.of(
                "status", estado.value(),
                "error", estado.getReasonPhrase(),
                "mensaje", mensaje != null ? mensaje : estado.getReasonPhrase());
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
