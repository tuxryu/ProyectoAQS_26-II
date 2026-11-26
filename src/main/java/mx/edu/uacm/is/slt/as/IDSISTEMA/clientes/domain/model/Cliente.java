package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.exception.CurpInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;

/**
 * Entidad de dominio que representa un cliente titular de polizas.
 */
@Entity
public class Cliente {

    private static final int LONGITUD_CURP = 18;
    private static final int LONGITUD_MAXIMA_NOMBRE = 128;
    private static final int LONGITUD_MAXIMA_DIRECCION = 512;
    private static final int EDAD_MINIMA_ANOS = 18;
    private static final int EDAD_MAXIMA_ANOS = 100;
    private static final ZoneId ZONA_CDMX = ZoneId.of("America/Mexico_City");
    private static final char A_ACENTO = (char) 0xE1;
    private static final char I_ACENTO = (char) 0xED;
    private static final char N_TILDE = (char) 0xF1;

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
            throw new CurpInvalidaException("El CURP no puede ser nulo ni vacio.");
        }
        if (valorCurp.length() != LONGITUD_CURP) {
            throw new CurpInvalidaException(
                    "El CURP debe tener exactamente 18 caracteres.");
        }
        if (valorCurp.contains("/") || valorCurp.contains("\\")) {
            throw new CurpInvalidaException("El CURP no puede contener diagonales.");
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

    public void setCurp(String nuevoCurp) {
        validarCurp(nuevoCurp);
        this.curp = nuevoCurp;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nuevosNombres) {
        validarTextoObligatorio(nuevosNombres, "El nombre", LONGITUD_MAXIMA_NOMBRE);
        this.nombres = nuevosNombres;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String nuevoPrimerApellido) {
        validarTextoObligatorio(
                nuevoPrimerApellido, "El primer apellido", LONGITUD_MAXIMA_NOMBRE);
        this.primerApellido = nuevoPrimerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String nuevoSegundoApellido) {
        validarSegundoApellido(nuevoSegundoApellido);
        this.segundoApellido = nuevoSegundoApellido;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String nuevaDireccion) {
        validarTextoObligatorio(
                nuevaDireccion, "La direccion", LONGITUD_MAXIMA_DIRECCION);
        this.direccion = nuevaDireccion;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate nuevaFechaNacimiento) {
        if (nuevaFechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula.");
        }
        this.fechaNacimiento = nuevaFechaNacimiento;
    }

    /**
     * Valida que la fecha de nacimiento del cliente para alta o modificacion no
     * sea futura, no exceda los 100 anos de antiguedad y corresponda a una
     * persona mayor de edad (al menos 18 anos cumplidos en zona CDMX).
     *
     * @param fechaNacimiento fecha de nacimiento a validar
     */
    public static void validarFechaNacimientoParaRegistro(LocalDate fechaNacimiento) {
        validarFechaNacimientoParaRegistro(fechaNacimiento, LocalDate.now(ZONA_CDMX));
    }

    /**
     * Valida la fecha de nacimiento del cliente respecto a una fecha de
     * referencia (sin fechas futuras, edad entre 18 y 100 anos).
     *
     * @param fechaNacimiento fecha de nacimiento a validar
     * @param fechaReferencia fecha actual de referencia
     */
    public static void validarFechaNacimientoParaRegistro(
            LocalDate fechaNacimiento, LocalDate fechaReferencia) {
        if (fechaNacimiento == null) {
            throw new ReglaNegocioException("La fecha de nacimiento es obligatoria.");
        }
        LocalDate hoy = Objects.requireNonNull(
                fechaReferencia, "La fecha de referencia es obligatoria.");
        if (fechaNacimiento.isAfter(hoy)) {
            throw new ReglaNegocioException(
                    "La fecha de nacimiento no puede ser una fecha futura.");
        }
        LocalDate fechaMinima = hoy.minusYears(EDAD_MAXIMA_ANOS);
        if (fechaNacimiento.isBefore(fechaMinima)) {
            throw new ReglaNegocioException(
                    "La fecha de nacimiento no puede superar los " + EDAD_MAXIMA_ANOS
                    + " a" + N_TILDE + "os de antiguedad (fecha m" + I_ACENTO
                    + "nima permitida: " + fechaMinima + ").");
        }
        LocalDate fechaMaxima = hoy.minusYears(EDAD_MINIMA_ANOS);
        if (fechaNacimiento.isAfter(fechaMaxima)) {
            throw new ReglaNegocioException(
                    "El cliente debe ser mayor de edad (m" + I_ACENTO + "nimo "
                    + EDAD_MINIMA_ANOS + " a" + N_TILDE + "os; fecha m" + A_ACENTO
                    + "xima permitida: " + fechaMaxima + ").");
        }
    }

    /**
     * Calcula la fecha minima permitida para registrar un cliente (hoy menos
     * 100 anos en zona CDMX).
     *
     * @return fecha minima permitida
     */
    public static LocalDate fechaMinimaRegistro() {
        return LocalDate.now(ZONA_CDMX).minusYears(EDAD_MAXIMA_ANOS);
    }

    /**
     * Calcula la fecha maxima permitida para registrar un cliente (hoy menos
     * 18 anos en zona CDMX).
     *
     * @return fecha maxima permitida
     */
    public static LocalDate fechaMaximaRegistro() {
        return LocalDate.now(ZONA_CDMX).minusYears(EDAD_MINIMA_ANOS);
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
