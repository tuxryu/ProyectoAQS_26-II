package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;

/**
 * Puerto de entrada para los casos de uso de consulta de beneficiarios.
 */
public interface ConsultaBeneficiarioUseCase {

    /**
     * Consulta un beneficiario especifico por su llave compuesta dentro de una
     * poliza.
     *
     * @param clavePoliza clave UUID de la poliza
     * @param nombres nombres del beneficiario
     * @param primerApellido primer apellido del beneficiario
     * @param segundoApellido segundo apellido del beneficiario (opcional)
     * @param fechaNacimiento fecha de nacimiento del beneficiario
     * @return el beneficiario encontrado
     */
    Beneficiario consultarPorDatos(
            UUID clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellido,
            LocalDate fechaNacimiento);

    /**
     * Lista todos los beneficiarios asociados a una poliza.
     *
     * @param clavePoliza clave UUID de la poliza
     * @return lista de beneficiarios de la poliza
     */
    List<Beneficiario> listarPorPoliza(UUID clavePoliza);
}
