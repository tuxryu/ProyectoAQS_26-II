package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del mapeo estricto de resiliencia y excepciones de
 * infraestructura en {@link TraductorExcepcionesSistemaDueno}.
 */
class TraductorExcepcionesSistemaDuenoTest {

    @Test
    void debeDevolverResultadoCuandoOperacionTieneExito() {
        String resultado = TraductorExcepcionesSistemaDueno.ejecutar(() -> "ok");
        assertEquals("ok", resultado);
    }

    @Test
    void debeEjecutarAccionSinRetornoCuandoTieneExito() {
        AtomicBoolean ejecutado = new AtomicBoolean(false);
        TraductorExcepcionesSistemaDueno.ejecutarAccion(() -> ejecutado.set(true));
        assertTrue(ejecutado.get());
    }

    @Test
    void debeMapearError502ASistemaDuenoNoDisponibleException() {
        RestClientResponseException error502 = crearErrorServidor(HttpStatus.BAD_GATEWAY);

        SistemaDuenoNoDisponibleException ex = assertThrows(
                SistemaDuenoNoDisponibleException.class,
                () -> TraductorExcepcionesSistemaDueno.ejecutar(() -> {
                    throw error502;
                }));

        assertSame(error502, ex.getCause());
    }

    @Test
    void debeMapearError503ASistemaDuenoNoDisponibleException() {
        RestClientResponseException error503 = crearErrorServidor(HttpStatus.SERVICE_UNAVAILABLE);

        SistemaDuenoNoDisponibleException ex = assertThrows(
                SistemaDuenoNoDisponibleException.class,
                () -> TraductorExcepcionesSistemaDueno.ejecutar(() -> {
                    throw error503;
                }));

        assertSame(error503, ex.getCause());
    }

    @Test
    void debeMapearError504ASistemaDuenoNoDisponibleException() {
        RestClientResponseException error504 = crearErrorServidor(HttpStatus.GATEWAY_TIMEOUT);

        SistemaDuenoNoDisponibleException ex = assertThrows(
                SistemaDuenoNoDisponibleException.class,
                () -> TraductorExcepcionesSistemaDueno.ejecutar(() -> {
                    throw error504;
                }));

        assertSame(error504, ex.getCause());
    }

    @Test
    void debeMapearCaidaDeRedOTimeoutASistemaDuenoNoDisponibleException() {
        ResourceAccessException caidaRed = new ResourceAccessException(
                "I/O error on GET request",
                new ConnectException("Connection refused"));
        ResourceAccessException timeout = new ResourceAccessException(
                "Read timed out",
                new SocketTimeoutException("Read timed out"));

        SistemaDuenoNoDisponibleException exRed = assertThrows(
                SistemaDuenoNoDisponibleException.class,
                () -> TraductorExcepcionesSistemaDueno.ejecutar(() -> {
                    throw caidaRed;
                }));
        SistemaDuenoNoDisponibleException exTimeout = assertThrows(
                SistemaDuenoNoDisponibleException.class,
                () -> TraductorExcepcionesSistemaDueno.ejecutar(() -> {
                    throw timeout;
                }));

        assertSame(caidaRed, exRed.getCause());
        assertSame(timeout, exTimeout.getCause());
    }

    @Test
    void debeMapearError500ASistemaDuenoRechazoException() {
        RestClientResponseException error500
                = crearErrorServidor(HttpStatus.INTERNAL_SERVER_ERROR);

        SistemaDuenoRechazoException ex = assertThrows(
                SistemaDuenoRechazoException.class,
                () -> TraductorExcepcionesSistemaDueno.ejecutar(() -> {
                    throw error500;
                }));

        assertSame(error500, ex.getCause());
    }

    @Test
    void noDebeMapearOtrosCodigosHttpANoDisponibleNiRechazo() {
        RestClientResponseException error400 = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8);
        RestClientResponseException error501 = crearErrorServidor(HttpStatus.NOT_IMPLEMENTED);

        RestClientResponseException propagada400 = assertThrows(
                RestClientResponseException.class,
                () -> TraductorExcepcionesSistemaDueno.ejecutar(() -> {
                    throw error400;
                }));
        RestClientResponseException propagada501 = assertThrows(
                RestClientResponseException.class,
                () -> TraductorExcepcionesSistemaDueno.ejecutar(() -> {
                    throw error501;
                }));

        assertSame(error400, propagada400);
        assertSame(error501, propagada501);
    }

    private RestClientResponseException crearErrorServidor(HttpStatus estado) {
        return HttpServerErrorException.create(
                estado,
                estado.getReasonPhrase(),
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8);
    }
}
