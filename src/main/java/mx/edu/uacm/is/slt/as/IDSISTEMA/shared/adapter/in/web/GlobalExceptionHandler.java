package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.adapter.in.web.dto.ErrorResponse;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.time.Instant;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> crearRespuesta(
            HttpStatus status,
            String mensaje,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }

    // 400 - Solicitud incorrecta
    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> manejarSolicitudIncorrecta(
            Exception ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "La solicitud contiene datos inválidos.",
                request
        );
    }

    // 404 - Recurso no encontrado
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(
            NoSuchElementException ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.NOT_FOUND,
                "No se encontró el recurso solicitado.",
                request
        );
    }
    // 409 - Conflicto de datos
    @ExceptionHandler(ConflictoDatosException.class)
    public ResponseEntity<ErrorResponse> manejarConflicto(
            ConflictoDatosException ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.CONFLICT,
                "La operación entra en conflicto con los datos existentes.",
                request
        );
    }

    // 422 - Datos válidos sintácticamente, pero no procesables
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarDatosNoProcesables(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Los datos enviados no cumplen las reglas de validación.",
                request
        );
    }

    // 502 - El Sistema Dueño rechazó la operación
    @ExceptionHandler(SistemaDuenoRechazoException.class)
    public ResponseEntity<ErrorResponse> manejarRechazoSistemaDueno(
            SistemaDuenoRechazoException ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.BAD_GATEWAY,
                "El Sistema Dueño rechazó la operación.",
                request
        );
    }

    // 503 - El Sistema Dueño no está disponible
    @ExceptionHandler(SistemaDuenoNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> manejarSistemaDuenoNoDisponible(
            SistemaDuenoNoDisponibleException ex, HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.SERVICE_UNAVAILABLE,
                "El Sistema Dueño no está disponible temporalmente.",
                request
        );
    }
}