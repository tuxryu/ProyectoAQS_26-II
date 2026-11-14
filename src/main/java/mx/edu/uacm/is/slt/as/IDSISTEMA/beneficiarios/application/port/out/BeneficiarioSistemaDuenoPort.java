package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;

/**
 * Puerto de salida hacia el Sistema Dueno para operaciones de beneficiarios.
 */
public interface BeneficiarioSistemaDuenoPort {

    /**
     * Busca un beneficiario especifico por su llave compuesta.
     *
     * @param fechaNacimiento fecha de nacimiento del beneficiario
     * @param clavePoliza clave UUID de la poliza
     * @param nombres nombres del beneficiario
     * @param primerApellido primer apellido del beneficiario
     * @param segundoApellido segundo apellido del beneficiario (opcional)
     * @return Optional con el beneficiario encontrado o vacio si no existe
     */
    Optional<Beneficiario> buscarPorDatos(
            LocalDate fechaNacimiento,
            UUID clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellido);

    /**
     * Lista todos los beneficiarios registrados para la poliza indicada
     * consumiendo el endpoint GET /beneficiarios del Sistema Dueno.
     *
     * @param clavePoliza clave UUID de la poliza
     * @return lista de beneficiarios vinculados a la poliza
     */
    List<Beneficiario> listarPorPoliza(UUID clavePoliza);

    /**
     * Registra un beneficiario en el Sistema Dueno mediante POST.
     *
     * @param beneficiario entidad de dominio a registrar
     * @return beneficiario devuelto por el Sistema Dueno
     */
    Beneficiario guardar(Beneficiario beneficiario);
}
