package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.in.web.dto;
import java.time.LocalDate;
import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
/**
 * DTO de respuesta para los endpoints REST del modulo de beneficiarios.
 *
 * @param clavePoliza identificador UUID de la poliza
 * @param nombres nombres del beneficiario
 * @param primerApellido primer apellido del beneficiario
 * @param segundoApellido segundo apellido del beneficiario
 * @param fechaNacimiento fecha de nacimiento del beneficiario
 * @param porcentaje porcentaje asignado (1..100)
 */

public record BeneficiarioRespuestaDto(
        UUID clavePoliza,
        String nombres,
        String primerApellido,
        String segundoApellido,
        LocalDate fechaNacimiento,
        int porcentaje) {
    /**
     * Convierte una entidad de dominio {@link Beneficiario} a DTO de respuesta.
     *
     * @param beneficiario entidad de dominio
     * @return DTO de respuesta
     */
    public static BeneficiarioRespuestaDto desde(Beneficiario beneficiario) {
        return new BeneficiarioRespuestaDto(
                beneficiario.getClavePoliza(),
                beneficiario.getNombres(),
                beneficiario.getPrimerApellido(),
                beneficiario.getSegundoApellido(),
                beneficiario.getFechaNacimiento(),
                beneficiario.getPorcentaje());
    }
}
