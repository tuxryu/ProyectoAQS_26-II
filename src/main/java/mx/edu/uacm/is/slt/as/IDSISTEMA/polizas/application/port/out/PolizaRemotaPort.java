package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;

/**
 * Puerto de salida hacia el Sistema Dueno para las lecturas de polizas.
 * Las operaciones de escritura (guardar, actualizar, eliminar) se agregaran con
 * sus casos de uso.
 */
public interface PolizaRemotaPort {

    Optional<Poliza> buscarPorClave(UUID clave);

    List<Poliza> listarTodas();

    List<Poliza> buscarPorCurp(String curp);

    List<Poliza> buscarPorTipo(int tipo);

    List<Poliza> buscarPorCliente(
            String nombres, String primerApellido, String segundoApellido);

    List<Poliza> buscarPorBeneficiario(
            String nombres, String primerApellido, String segundoApellido);
}
