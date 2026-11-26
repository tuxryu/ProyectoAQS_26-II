package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in;

import java.time.LocalDate;
import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;

/**
 * Puerto de entrada para la modificacion individual de un beneficiario
 * mediante operacion compuesta (respaldo en memoria, validacion de cupo y
 * duplicidad, reinicio de poliza cuando cambia la llave compuesta y
 * restauracion verificada con relectura).
 */
public interface ModificacionBeneficiarioUseCase {

    /**
     * Actualiza los datos de un beneficiario existente en una poliza, validando
     * que no haya duplicidad ni se exceda el 100% acumulado.
     *
     * @param clavePoliza clave UUID de la poliza
     * @param nombresActuales nombres actuales del beneficiario
     * @param primerApellidoActual primer apellido actual del beneficiario
     * @param segundoApellidoActual segundo apellido actual (opcional)
     * @param fechaNacimientoActual fecha de nacimiento actual
     * @param beneficiarioActualizado nuevos datos del beneficiario
     * @return beneficiario actualizado y verificado por relectura
     */
    Beneficiario actualizar(
            UUID clavePoliza,
            String nombresActuales,
            String primerApellidoActual,
            String segundoApellidoActual,
            LocalDate fechaNacimientoActual,
            Beneficiario beneficiarioActualizado);
}
