package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto de entrada para la baja individual de un beneficiario mediante
 * operacion compuesta (respaldo en memoria, baja en cascada de poliza,
 * recreacion de poliza con la misma clave UUID y restauracion de los
 * beneficiarios restantes).
 */
public interface BajaBeneficiarioUseCase {

    /**
     * Elimina un beneficiario especifico de una poliza conservando la poliza y
     * el resto de sus beneficiarios.
     *
     * @param clavePoliza clave UUID de la poliza
     * @param nombres nombres del beneficiario a eliminar
     * @param primerApellido primer apellido del beneficiario a eliminar
     * @param segundoApellido segundo apellido del beneficiario (opcional)
     * @param fechaNacimiento fecha de nacimiento del beneficiario
     */
    void eliminar(
            UUID clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellido,
            LocalDate fechaNacimiento);
}
