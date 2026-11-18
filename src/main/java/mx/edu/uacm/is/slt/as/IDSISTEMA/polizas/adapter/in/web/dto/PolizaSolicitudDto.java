package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.in.web.dto;

/**
 * DTO de entrada JSON para crear (POST /polizas) o modificar parcialmente
 * (PATCH /polizas/{clave}) una poliza. En creacion son obligatorios tipo,
 * monto y curpCliente; en modificacion parcial todos los campos son opcionales
 * y los que vengan en null conservan el valor actual de la poliza.
 *
 * @param tipo tipo de poliza (0 = Auto, 1 = Vida, 2 = Medico)
 * @param monto monto asegurado (mayor a 0)
 * @param descripcion descripcion de la cobertura
 * @param curpCliente CURP del cliente titular
 */
public record PolizaSolicitudDto(
        Integer tipo,
        Float monto,
        String descripcion,
        String curpCliente) {
}
