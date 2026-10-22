package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Embeddable;

/**
 * Identificador compuesto del beneficiario.
 */
@Embeddable
public class BeneficiarioId implements Serializable {

    private static final int LONGITUD_MAXIMA_NOMBRE = 128;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private LocalDate fechaNacimiento;
    private UUID clavePoliza;

    protected BeneficiarioId() {
    }

    public BeneficiarioId(
            String nombres,
            String primerApellido,
            String segundoApellido,
            LocalDate fechaNacimiento,
            UUID clavePoliza) {
        validarTextoObligatorio(nombres, "El nombre");
        validarTextoObligatorio(primerApellido, "El primer apellido");
        validarSegundoApellido(segundoApellido);
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (clavePoliza == null) {
            throw new IllegalArgumentException("La clave de la poliza es obligatoria.");
        }
        this.nombres = nombres;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.fechaNacimiento = fechaNacimiento;
        this.clavePoliza = clavePoliza;
    }

    private void validarTextoObligatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " no puede ser nulo ni vacio.");
        }
        if (valor.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new IllegalArgumentException(
                    campo + " excede los " + LONGITUD_MAXIMA_NOMBRE + " caracteres.");
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

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public UUID getClavePoliza() {
        return clavePoliza;
    }

    public void setClavePoliza(UUID clavePoliza) {
        this.clavePoliza = clavePoliza;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BeneficiarioId other)) {
            return false;
        }
        return Objects.equals(nombres, other.nombres)
                && Objects.equals(primerApellido, other.primerApellido)
                && Objects.equals(segundoApellido, other.segundoApellido)
                && Objects.equals(fechaNacimiento, other.fechaNacimiento)
                && Objects.equals(clavePoliza, other.clavePoliza);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                nombres, primerApellido, segundoApellido, fechaNacimiento, clavePoliza);
    }
}
