package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Entidad de dominio que representa un cliente titular de polizas.
 */
@Entity
public class Cliente {

    private static final int LONGITUD_CURP = 18;
    private static final int LONGITUD_MAXIMA_NOMBRE = 128;
    private static final int LONGITUD_MAXIMA_DIRECCION = 512;

    @Id
    private String curp;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String direccion;
    private LocalDate fechaNacimiento;

    protected Cliente() {
    }

    public Cliente(
            String curp,
            String nombres,
            String primerApellido,
            String segundoApellido,
            String direccion,
            LocalDate fechaNacimiento) {
        validarCurp(curp);
        validarTextoObligatorio(nombres, "El nombre", LONGITUD_MAXIMA_NOMBRE);
        validarTextoObligatorio(
                primerApellido, "El primer apellido", LONGITUD_MAXIMA_NOMBRE);
        validarSegundoApellido(segundoApellido);
        validarTextoObligatorio(
                direccion, "La direccion", LONGITUD_MAXIMA_DIRECCION);
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula.");
        }

        this.curp = curp;
        this.nombres = nombres;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
    }

    private void validarCurp(String valorCurp) {
        if (valorCurp == null || valorCurp.trim().isEmpty()) {
            throw new IllegalArgumentException("El CURP no puede ser nulo ni vacio.");
        }
        if (valorCurp.length() != LONGITUD_CURP) {
            throw new IllegalArgumentException(
                    "El CURP debe tener exactamente 18 caracteres.");
        }
        if (valorCurp.contains("/") || valorCurp.contains("\\")) {
            throw new IllegalArgumentException("El CURP no puede contener diagonales.");
        }
    }

    private void validarTextoObligatorio(String valor, String campo, int longitudMaxima) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " no puede ser nulo ni vacio.");
        }
        if (valor.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    campo + " excede los " + longitudMaxima + " caracteres.");
        }
        if (valor.contains("/") || valor.contains("\\")) {
            throw new IllegalArgumentException(campo + " no puede contener diagonales.");
        }
    }

    private void validarSegundoApellido(String valor) {
        if (valor != null) {
            if (valor.length() > LONGITUD_MAXIMA_NOMBRE) {
                throw new IllegalArgumentException(
                        "El segundo apellido excede los "
                                + LONGITUD_MAXIMA_NOMBRE + " caracteres.");
            }
            if (valor.contains("/") || valor.contains("\\")) {
                throw new IllegalArgumentException(
                        "El segundo apellido no puede contener diagonales.");
            }
        }
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        validarCurp(curp);
        this.curp = curp;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        validarTextoObligatorio(nombres, "El nombre", LONGITUD_MAXIMA_NOMBRE);
        this.nombres = nombres;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        validarTextoObligatorio(
                primerApellido, "El primer apellido", LONGITUD_MAXIMA_NOMBRE);
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        validarSegundoApellido(segundoApellido);
        this.segundoApellido = segundoApellido;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        validarTextoObligatorio(
                direccion, "La direccion", LONGITUD_MAXIMA_DIRECCION);
        this.direccion = direccion;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Cliente other)) {
            return false;
        }
        return Objects.equals(curp, other.curp)
                && Objects.equals(nombres, other.nombres)
                && Objects.equals(primerApellido, other.primerApellido)
                && Objects.equals(segundoApellido, other.segundoApellido)
                && Objects.equals(direccion, other.direccion)
                && Objects.equals(fechaNacimiento, other.fechaNacimiento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                curp, nombres, primerApellido, segundoApellido, direccion, fechaNacimiento);
    }
}



