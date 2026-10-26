
package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.adapter.in.web;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.servlet.http.HttpServletRequest;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/prueba");
    }

    @Test
    void debeMapearErrores400() {
        assertEquals(
                HttpStatus.BAD_REQUEST,
                handler.manejarSolicitudIncorrecta(
                        new IllegalArgumentException(), request
                ).getStatusCode()
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                handler.manejarSolicitudIncorrecta(
                        new HttpMessageNotReadableException(
                                "JSON inválido", null, null
                        ), request
                ).getStatusCode()
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                handler.manejarSolicitudIncorrecta(
                        new MissingServletRequestParameterException(
                                "id", "String"
                        ), request
                ).getStatusCode()
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                handler.manejarSolicitudIncorrecta(
                        mock(MethodArgumentTypeMismatchException.class), request
                ).getStatusCode()
        );
    }

    @Test
    void debeMapearError404() {
        assertEquals(
                HttpStatus.NOT_FOUND,
                handler.manejarNoEncontrado(
                        new NoSuchElementException(), request
                ).getStatusCode()
        );
    }

    @Test
    void debeMapearError409() {
        assertEquals(
                HttpStatus.CONFLICT,
                handler.manejarConflicto(
                        new ConflictoDatosException("Conflicto"), request
                ).getStatusCode()
        );
    }

    @Test
    void debeMapearError422() {
        MethodArgumentNotValidException ex
                = mock(MethodArgumentNotValidException.class);

        assertEquals(
                HttpStatus.UNPROCESSABLE_ENTITY,
                handler.manejarDatosNoProcesables(ex, request).getStatusCode()
        );
    }

    @Test
    void debeMapearError502() {
        assertEquals(
                HttpStatus.BAD_GATEWAY,
                handler.manejarRechazoSistemaDueno(
                        new SistemaDuenoRechazoException("Rechazo"), request
                ).getStatusCode()
        );
    }

    @Test
    void debeMapearError503() {
        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                handler.manejarSistemaDuenoNoDisponible(
                        new SistemaDuenoNoDisponibleException("No disponible"),
                        request
                ).getStatusCode()
        );
    }

    @Test
    void debeConstruirRespuestaDeErrorConTodosLosCampos() {
        var respuesta = handler.manejarNoEncontrado(
                new NoSuchElementException(),
                request
        );

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        var error = respuesta.getBody();

        assertNotNull(error.timestamp());
        assertEquals(404, error.status());
        assertEquals("Not Found", error.error());
        assertEquals("No se encontró el recurso solicitado.", error.message());
        assertEquals("/prueba", error.path());
    }

    @Test
    void debeMantenerLaEstructuraDeRespuestaEnError422() {
        MethodArgumentNotValidException ex
                = mock(MethodArgumentNotValidException.class);

        var respuesta = handler.manejarDatosNoProcesables(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        var error = respuesta.getBody();

        assertNotNull(error.timestamp());
        assertEquals(422, error.status());
        assertEquals("Unprocessable Entity", error.error());
        assertEquals(
                "Los datos enviados no cumplen las reglas de validación.",
                error.message()
        );
        assertEquals("/prueba", error.path());
    }
}
