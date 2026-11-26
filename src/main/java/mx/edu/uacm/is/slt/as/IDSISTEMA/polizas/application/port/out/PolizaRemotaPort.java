package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;

/**
 * Puerto de salida hacia el Sistema Dueno para las lecturas y escrituras de
 * polizas.
 */
public interface PolizaRemotaPort {

    Optional<Poliza> buscarPorClave(UUID clave);

    List<Poliza> listarTodas();

    List<Poliza> buscarPorCurp(String curp);

    List<Poliza> buscarPorTipo(int tipo);

    List<Poliza> buscarPorFechaNacimientoBeneficiario(LocalDate fechaNacimiento);

    List<Poliza> buscarPorCliente(
            String nombres, String primerApellido, String segundoApellido);

    List<Poliza> buscarPorBeneficiario(
            String nombres, String primerApellido, String segundoApellido);

    Poliza guardar(Poliza poliza);

    /**
     * Actualiza una poliza existente en el Sistema Dueno.
     *
     * @param clave  clave de la poliza a actualizar
     * @param poliza nuevos datos de la poliza
     */
    Poliza actualizar(UUID clave, Poliza poliza);

    /**
     * Elimina una poliza por su clave en el Sistema Dueno.
     *
     * @param clave clave de la poliza a eliminar
     */
    void eliminar(UUID clave);
}
