package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in;

import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;

/**
 * Puerto de entrada para el caso de uso de creacion de polizas.
 */
public interface CrearPolizaUseCasePort {

    /**
     * Registra una nueva poliza en el sistema.
     *
     * @param poliza entidad de dominio a registrar
     * @return poliza registrada
     */
    Poliza crearPoliza(Poliza poliza);
}
