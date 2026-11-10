package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http;

import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;

/**
 * Traductor centralizado de resiliencia y excepciones de infraestructura para
 * las llamadas HTTP hacia el Sistema Dueno.
 *
 * <p>Aplica el mapeo estricto de errores:
 * <ul>
 *   <li>Solo HTTP 502, 503, 504 y caidas de red o timeouts
 *       ({@link ResourceAccessException}) se traducen a
 *       {@link SistemaDuenoNoDisponibleException}.</li>
 *   <li>Solo HTTP 500 (rechazo de datos en el servidor remoto) se traduce a
 *       {@link SistemaDuenoRechazoException}.</li>
 *   <li>Cualquier otro codigo HTTP inesperado se propaga tal cual sin
 *       enmascararse como indisponibilidad ni rechazo por datos.</li>
 * </ul>
 */
public final class TraductorExcepcionesSistemaDueno {

    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    private static final int HTTP_BAD_GATEWAY = 502;

    private static final int HTTP_SERVICE_UNAVAILABLE = 503;

    private static final int HTTP_GATEWAY_TIMEOUT = 504;

    private TraductorExcepcionesSistemaDueno() {
    }

    /**
     * Ejecuta una llamada hacia el Sistema Dueno aplicando el mapeo estricto
     * de resiliencia y excepciones de infraestructura.
     *
     * @param <T> tipo de retorno de la operacion
     * @param operacion invocacion HTTP hacia el Sistema Dueno
     * @return resultado devuelto por la operacion
     * @throws SistemaDuenoNoDisponibleException si ocurre error 502, 503, 504
     *         o fallo de conexion/timeout
     * @throws SistemaDuenoRechazoException si el Sistema Dueno responde HTTP 500
     */
    public static <T> T ejecutar(Supplier<T> operacion) {
        Objects.requireNonNull(
                operacion, "La operacion hacia el Sistema Dueno es obligatoria.");
        try {
            return operacion.get();
        } catch (ResourceAccessException ex) {
            throw traducirFalloRed(ex);
        } catch (RestClientResponseException ex) {
            throw traducirErrorHttp(ex);
        }
    }

    /**
     * Ejecuta una llamada sin valor de retorno hacia el Sistema Dueno
     * aplicando el mapeo estricto de resiliencia.
     *
     * @param operacion invocacion HTTP sin retorno hacia el Sistema Dueno
     */
    public static void ejecutarAccion(Runnable operacion) {
        Objects.requireNonNull(
                operacion, "La operacion hacia el Sistema Dueno es obligatoria.");
        ejecutar(() -> {
            operacion.run();
            return null;
        });
    }

    /**
     * Traduce una caida de red o timeout ({@link ResourceAccessException}) a
     * {@link SistemaDuenoNoDisponibleException}.
     *
     * @param ex excepcion de acceso a recursos de red
     * @return excepcion de infraestructura que indica indisponibilidad (503)
     */
    public static SistemaDuenoNoDisponibleException traducirFalloRed(
            ResourceAccessException ex) {
        Objects.requireNonNull(
                ex, "La excepcion de acceso a recursos es obligatoria.");
        return new SistemaDuenoNoDisponibleException(
                "El Sistema Dueno no esta disponible por fallo de conexion o timeout.",
                ex);
    }

    /**
     * Traduce de forma estricta un error HTTP del Sistema Dueno: solo 502, 503
     * y 504 a {@link SistemaDuenoNoDisponibleException}, y 500 a
     * {@link SistemaDuenoRechazoException}. Otros codigos HTTP se devuelven sin
     * alterar.
     *
     * @param ex excepcion de respuesta HTTP de RestClient
     * @return excepcion traducida o la misma excepcion original si no aplica
     */
    public static RuntimeException traducirErrorHttp(
            RestClientResponseException ex) {
        Objects.requireNonNull(
                ex, "La excepcion de respuesta HTTP es obligatoria.");
        int codigo = ex.getStatusCode().value();
        if (codigo == HTTP_BAD_GATEWAY
                || codigo == HTTP_SERVICE_UNAVAILABLE
                || codigo == HTTP_GATEWAY_TIMEOUT) {
            return new SistemaDuenoNoDisponibleException(
                    "El Sistema Dueno no esta disponible (HTTP " + codigo + ").",
                    ex);
        }
        if (codigo == HTTP_INTERNAL_SERVER_ERROR) {
            return new SistemaDuenoRechazoException(
                    "El Sistema Dueno rechazo la peticion (HTTP 500).",
                    ex);
        }
        return ex;
    }
}
