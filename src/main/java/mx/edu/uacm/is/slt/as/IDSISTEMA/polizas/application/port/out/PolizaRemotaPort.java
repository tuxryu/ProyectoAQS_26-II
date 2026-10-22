package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out;

import java.util.Optional;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;

/**
 * Puerto de salida hacia el Sistema Dueño para operaciones de Pólizas.
 */
public interface PolizaRemotaPort {

    Optional<Poliza> consultarPorId(String id);

    Poliza registrarPoliza(Poliza poliza);

    Poliza actualizarPoliza(Poliza poliza);
}