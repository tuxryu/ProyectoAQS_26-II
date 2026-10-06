package mx.uacm.aqs.clientes.domain.model;

import java.util.regex.Pattern;

public record Cliente(String curp, String nombre, String apellidoPaterno, String apellidoMaterno) {

    private static final Pattern CURP_PATTERN =
            Pattern.compile("^[A-Z0-9]{18}$");

    public Cliente {
        validarCampo(curp, "curp");
        validarCampo(nombre, "nombre");
        validarCampo(apellidoPaterno, "apellidoPaterno");
        validarCampo(apellidoMaterno, "apellidoMaterno");
        if (!CURP_PATTERN.matcher(curp).matches()) {
            throw new IllegalArgumentException("La CURP debe contener 18 caracteres alfanumericos en mayusculas");
        }
    }

    private static void validarCampo(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
    }
}
