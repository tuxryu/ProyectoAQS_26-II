package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in;

import java.util.UUID;

/**
 * Puerto de entrada para el caso de uso de cancelacion de polizas.
 */
public interface CancelarPolizaUseCasePort {

    /**
     * Cancela una poliza existente por su clave UUID.
     *
     * @param id clave UUID de la poliza a cancelar
     */
    void cancelarPoliza(UUID id);
}
