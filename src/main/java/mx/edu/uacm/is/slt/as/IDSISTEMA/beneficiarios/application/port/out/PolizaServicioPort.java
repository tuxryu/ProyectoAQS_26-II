package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out;

import java.util.UUID;

/**
 * Puerto de salida inter-modulo para consultar existencia de polizas y ejecutar
 * el reinicio controlado de una poliza (respaldo en memoria, baja en cascada y
 * recreacion conservando la clave UUID) para baja y edicion de beneficiarios.
 */
public interface PolizaServicioPort {

    /**
     * Verifica si existe una poliza registrada con la clave indicada.
     *
     * @param clave clave UUID de la poliza
     * @return {@code true} si existe en el Sistema Dueno
     */
    boolean existePolizaPorClave(UUID clave);

    /**
     * Respalda los datos de la poliza en memoria, ejecuta su eliminacion en
     * cascada y la vuelve a registrar conservando la misma clave UUID.
     *
     * @param clave clave UUID de la poliza a reiniciar
     */
    default void reiniciarPolizaConservandoClave(UUID clave) {
        throw new UnsupportedOperationException(
                "El puerto PolizaServicioPort no soporta reinicio de poliza.");
    }
}
