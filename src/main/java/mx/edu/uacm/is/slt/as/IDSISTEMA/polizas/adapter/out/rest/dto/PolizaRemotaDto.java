package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.out.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO en camelCase con los datos de una poliza tal como los devuelve el
 * Sistema Dueno (clave, tipo, monto, descripcion y curpCliente).
 *
 * @param clave identificador UUID de la poliza
 * @param tipo tipo de poliza (0 = Auto, 1 = Vida, 2 = Medico)
 * @param monto monto de cobertura
 * @param descripcion descripcion de la cobertura
 * @param curpCliente CURP del cliente titular, tal cual la guarda el servidor
 */
public record PolizaRemotaDto(
        UUID clave,
        int tipo,
        BigDecimal monto,
        String descripcion,
        String curpCliente) {
}
