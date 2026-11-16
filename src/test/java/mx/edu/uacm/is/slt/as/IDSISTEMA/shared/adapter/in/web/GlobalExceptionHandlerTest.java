package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.adapter.in.web;

import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.servlet.http.HttpServletRequest;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.FiltroCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.FiltroPoliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.FuncionalidadNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link GlobalExceptionHandler}, validacion de diagonales
 * (/ y \) y limites de longitud en filtros y entidades de dominio.
 */
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
                                "JSON invalido", null, null
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

        assertEquals(
                HttpStatus.NOT_FOUND,
                handler.manejarRecursoNoEncontrado(
                        new RecursoNoEncontradoException("No existe la poliza"),
                        request
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

        assertEquals(
                HttpStatus.UNPROCESSABLE_ENTITY,
                handler.manejarReglaNegocio(
                        new ReglaNegocioException("La CURP no puede contener / ni \\."),
                        request
                ).getStatusCode()
        );
    }

    @Test
    void debeMapearError501Y502Y503() {
        assertEquals(
                HttpStatus.NOT_IMPLEMENTED,
                handler.manejarNoImplementado(
                        new FuncionalidadNoDisponibleException("Pendiente"),
                        request
                ).getStatusCode()
        );

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                handler.manejarRechazoSistemaDueno(
                        new SistemaDuenoRechazoException("Rechazo"), request
                ).getStatusCode()
        );

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
        assertEquals("No se encontr\u00f3 el recurso solicitado.", error.message());
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
                "Los datos enviados no cumplen las reglas de validaci\u00f3n.",
                error.message()
        );
        assertEquals("/prueba", error.path());
    }

    @Test
    void nuncaDebeFiltrarStacktracesNiRutasInternasEnErrorResponse() {
        var conRutaRemota = handler.manejarReglaNegocio(
                new ReglaNegocioException(
                        "Fallo en http://nachintoch.mx:8080/polizas\n\tat mx.edu.uacm.Poliza"),
                request);
        assertNotNull(conRutaRemota.getBody());
        assertFalse(conRutaRemota.getBody().message().contains("nachintoch.mx"));
        assertFalse(conRutaRemota.getBody().message().contains("mx.edu.uacm"));
        assertFalse(conRutaRemota.getBody().message().contains("\tat "));

        var conRutaArchivo = handler.manejarRecursoNoEncontrado(
                new RecursoNoEncontradoException(
                        "NullPointerException en C:\\Users\\servidor\\app"),
                request);
        assertNotNull(conRutaArchivo.getBody());
        assertFalse(conRutaArchivo.getBody().message().contains("C:\\"));
        assertFalse(conRutaArchivo.getBody().message().contains("Exception"));

        var errorInterno = handler.manejarErrorInterno(
                new RuntimeException(
                        "org.springframework.web.client.ResourceAccessException: "
                        + "http://nachintoch.mx:8080/cliente"),
                request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorInterno.getStatusCode());
        assertNotNull(errorInterno.getBody());
        assertFalse(errorInterno.getBody().message().contains("nachintoch.mx"));
        assertFalse(errorInterno.getBody().message().contains("org.springframework"));
    }

    @Test
    void debeRechazarDiagonalesYExcesoDeLongitudCon422EnFiltrosYDominio() {
        String nombreExcesivo = "A".repeat(129);
        String descripcionExcesiva = "D".repeat(513);
        String curpExcesiva = "B".repeat(19);

        assertThrows(
                ReglaNegocioException.class,
                () -> FiltroCliente.desde("Juan/Pedro", "Perez", null));
        assertThrows(
                ReglaNegocioException.class,
                () -> FiltroCliente.desde("Juan", "Perez\\Lopez", null));
        assertThrows(
                ReglaNegocioException.class,
                () -> FiltroCliente.desde(nombreExcesivo, "Perez", null));

        assertThrows(
                ReglaNegocioException.class,
                () -> FiltroPoliza.desde(curpExcesiva, null, null));
        assertThrows(
                ReglaNegocioException.class,
                () -> new FiltroPoliza.PorCliente(nombreExcesivo, "Brown", null));
        assertThrows(
                ReglaNegocioException.class,
                () -> new FiltroPoliza.PorBeneficiario("Clara", nombreExcesivo, null));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Poliza(
                        UUID.randomUUID(),
                        (byte) 1,
                        1000.0f,
                        descripcionExcesiva,
                        "BROE550101HDFABC00"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Poliza(
                        UUID.randomUUID(),
                        (byte) 1,
                        1000.0f,
                        "Seguro",
                        curpExcesiva));
    }
}
