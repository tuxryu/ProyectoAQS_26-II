package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.in.web.dto;

import java.time.LocalDate;

/**
 * DTO de entrada para las operaciones de escritura de clientes (POST y PUT).
 *
 * @param curp CURP del cliente titular (18 caracteres)
 * @param nombres nombres del cliente
 * @param primerApellido primer apellido del cliente
 * @param segundoApellido segundo apellido del cliente (opcional)
 * @param direccion direccion de residencia del cliente
 * @param fechaNacimiento fecha de nacimiento del cliente (yyyy-MM-dd)
 */
public record ClienteSolicitudDto(
        String curp,
        String nombres,
        String primerApellido,
        String segundoApellido,
        String direccion,
        LocalDate fechaNacimiento) {
}
