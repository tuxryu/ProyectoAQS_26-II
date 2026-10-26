package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;

/**
 * DTO de respuesta para los endpoints REST del modulo de polizas.
 *
 * @param clave identificador UUID de la poliza
 * @param tipo tipo de poliza (0 = Auto, 1 = Vida, 2 = Medico)
 * @param monto monto de cobertura
 * @param descripcion descripcion de la cobertura
 * @param curpCliente CURP del cliente titular
 */
public record PolizaRespuestaDto(
        UUID clave,
        int tipo,
        BigDecimal monto,
        String descripcion,
        String curpCliente) {

    /**
     * Convierte una entidad de dominio {@link Poliza} a DTO de respuesta.
     *
     * @param poliza entidad de dominio
     * @return DTO de respuesta
     */
    public static PolizaRespuestaDto desde(Poliza poliza) {
        return new PolizaRespuestaDto(
                poliza.getClave(),
                poliza.getTipo(),
                BigDecimal.valueOf(poliza.getMonto()),
                poliza.getDescripcion(),
                poliza.getCurpCliente());
    }
}
