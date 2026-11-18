package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.adapter.in.web;

import java.time.Instant;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.servlet.http.HttpServletRequest;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.adapter.in.web.dto.ErrorResponse;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.FuncionalidadNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;

/**
 * Manejador global de excepciones para traducir errores de dominio e
 * infraestructura a respuestas ErrorResponse con los codigos HTTP de la
 * especificacion (400, 404, 409, 422, 500, 501, 502 y 503), garantizando que
 * nunca se filtren stacktraces, nombres de paquetes internos ni URLs del
 * Sistema Dueno.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final char A_ACENTO = (char) 0xE1;

    private static final char O_ACENTO = (char) 0xF3;

    private static final char N_TILDE = (char) 0xF1;

    private static final String MENSAJE_400
            = "La solicitud contiene datos inv" + A_ACENTO + "lidos.";

    private static final String MENSAJE_404
            = "No se encontr" + O_ACENTO + " el recurso solicitado.";

    private static final String MENSAJE_409
            = "La operaci" + O_ACENTO + "n entra en conflicto con los datos existentes.";

    private static final String MENSAJE_422
            = "Los datos enviados no cumplen las reglas de validaci" + O_ACENTO + "n.";

    private static final String MENSAJE_500
            = "Ocurri" + O_ACENTO + " un error interno al procesar la solicitud.";

    private static final String MENSAJE_502
            = "El Sistema Due" + N_TILDE + "o rechaz"
            + O_ACENTO + " la operaci" + O_ACENTO + "n.";

    private static final String MENSAJE_503
            = "El Sistema Due" + N_TILDE + "o no est"
            + A_ACENTO + " disponible temporalmente.";

    private ResponseEntity<ErrorResponse> crearRespuesta(
            HttpStatus status,
            String mensaje,
            HttpServletRequest request) {
        String rutaSegura = request != null && request.getRequestURI() != null
                ? request.getRequestURI()
                : "";
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                rutaSegura
        );
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Verifica que el mensaje de error no contenga trazas de pila, rutas de
     * archivos del servidor, nombres de clases internas ni la URL del Sistema
     * Dueno; en caso contrario devuelve el mensaje de respaldo.
     *
     * @param mensaje mensaje candidato de la excepcion
     * @param respaldo mensaje generico seguro
     * @return mensaje seguro para incluir en ErrorResponse
     */
    private String sanitizarMensaje(String mensaje, String respaldo) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return respaldo;
        }
        if (contieneInformacionInterna(mensaje)) {
            return respaldo;
        }
        return mensaje;
    }

    private boolean contieneInformacionInterna(String texto) {
        return contieneTrazaOPaquete(texto) || contieneRutaOUrlInterna(texto);
    }

    private boolean contieneTrazaOPaquete(String texto) {
        return texto.contains("\n")
                || texto.contains("\r")
                || texto.contains("\tat ")
                || texto.contains("Exception")
                || texto.contains("mx.edu.uacm")
                || texto.contains("org.springframework")
                || texto.contains("java.lang");
    }

    private boolean contieneRutaOUrlInterna(String texto) {
        return texto.contains("nachintoch.mx")
                || texto.contains("http://")
                || texto.contains("https://")
                || texto.contains("C:\\")
                || texto.contains("/home/");
    }

    // 400 - Solicitud incorrecta
    @ExceptionHandler({
        SolicitudInvalidaException.class,
        IllegalArgumentException.class,
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> manejarSolicitudIncorrecta(
            Exception ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                MENSAJE_400,
                request
        );
    }

    // 404 - Recurso no encontrado
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(
            NoSuchElementException ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.NOT_FOUND,
                MENSAJE_404,
                request
        );
    }

    // 404 - Recurso de dominio no encontrado (p. ej. PolizaNoEncontradaException)
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex, HttpServletRequest request) {
        String mensaje = ex != null
                ? sanitizarMensaje(ex.getMessage(), MENSAJE_404)
                : MENSAJE_404;
        return crearRespuesta(
                HttpStatus.NOT_FOUND,
                mensaje,
                request
        );
    }

    // 409 - Conflicto de datos
    @ExceptionHandler(ConflictoDatosException.class)
    public ResponseEntity<ErrorResponse> manejarConflicto(
            ConflictoDatosException ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.CONFLICT,
                MENSAJE_409,
                request
        );
    }

    // 422 - Datos validos sintacticamente, pero no procesables
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarDatosNoProcesables(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.UNPROCESSABLE_ENTITY,
                MENSAJE_422,
                request
        );
    }

    // 422 - Violacion de regla de negocio/dominio
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(
            ReglaNegocioException ex, HttpServletRequest request) {
        String mensaje = ex != null
                ? sanitizarMensaje(ex.getMessage(), MENSAJE_422)
                : MENSAJE_422;
        return crearRespuesta(
                HttpStatus.UNPROCESSABLE_ENTITY,
                mensaje,
                request
        );
    }

    // 501 - Funcionalidad pendiente de implementar
    @ExceptionHandler(FuncionalidadNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> manejarNoImplementado(
            FuncionalidadNoDisponibleException ex, HttpServletRequest request) {
        String respaldo = HttpStatus.NOT_IMPLEMENTED.getReasonPhrase();
        String mensaje = ex != null
                ? sanitizarMensaje(ex.getMessage(), respaldo)
                : respaldo;
        return crearRespuesta(
                HttpStatus.NOT_IMPLEMENTED,
                mensaje,
                request
        );
    }

    // 502 - El Sistema Dueno rechazo la operacion
    @ExceptionHandler(SistemaDuenoRechazoException.class)
    public ResponseEntity<ErrorResponse> manejarRechazoSistemaDueno(
            SistemaDuenoRechazoException ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.BAD_GATEWAY,
                MENSAJE_502,
                request
        );
    }

    // 503 - El Sistema Dueno no esta disponible
    @ExceptionHandler(SistemaDuenoNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> manejarSistemaDuenoNoDisponible(
            SistemaDuenoNoDisponibleException ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.SERVICE_UNAVAILABLE,
                MENSAJE_503,
                request
        );
    }

    // 500 - Cualquier error no controlado (sin filtrar stacktrace ni rutas internas)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorInterno(
            Exception ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                MENSAJE_500,
                request
        );
    }
}
