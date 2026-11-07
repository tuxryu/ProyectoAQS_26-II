package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.in.web.dto;
import java.time.LocalDate;
/**
 * DTO de entrada para registrar un beneficiario vinculado a una poliza.
 *
 * @param nombres nombres del beneficiario
 * @param primerApellido primer apellido del beneficiario
 * @param segundoApellido segundo apellido del beneficiario (opcional)
 * @param fechaNacimiento fecha de nacimiento del beneficiario (yyyy-MM-dd)
 * @param porcentaje porcentaje asignado (1..100)
 */
public record BeneficiarioSolicitudDto(
        String nombres,
        String primerApellido,
        String segundoApellido,
        LocalDate fechaNacimiento,
        Integer porcentaje) {
}
