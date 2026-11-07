package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.in.web.dto;

import java.time.LocalDate;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

/**
 * DTO de respuesta para los endpoints REST del modulo de clientes.
 *
 * @param curp CURP del cliente titular
 * @param nombres nombres del cliente
 * @param primerApellido primer apellido del cliente
 * @param segundoApellido segundo apellido del cliente (opcional)
 * @param direccion direccion de residencia del cliente
 * @param fechaNacimiento fecha de nacimiento del cliente
 */
public record ClienteRespuestaDto(
        String curp,
        String nombres,
        String primerApellido,
        String segundoApellido,
        String direccion,
        LocalDate fechaNacimiento) {

    /**
     * Convierte una entidad de dominio {@link Cliente} a DTO de respuesta.
     *
     * @param cliente entidad de dominio
     * @return DTO de respuesta
     */
    public static ClienteRespuestaDto desde(Cliente cliente) {
        return new ClienteRespuestaDto(
                cliente.getCurp(),
                cliente.getNombres(),
                cliente.getPrimerApellido(),
                cliente.getSegundoApellido(),
                cliente.getDireccion(),
                cliente.getFechaNacimiento());
    }
}