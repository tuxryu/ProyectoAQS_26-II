package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in;

import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;

/**
 * Puerto de entrada para el caso de uso de modificacion de polizas.
 */
public interface ModificacionPolizaUseCasePort {

    /**
     * Modifica la informacion de una poliza existente en el sistema.
     *
     * @param clave la clave UUID de la poliza a modificar
     * @param polizaConCambios objeto de dominio con los datos actualizados
     * @return la poliza modificada y actualizada
     */
    Poliza modificarPoliza(UUID clave, Poliza polizaConCambios);
}
