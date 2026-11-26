package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.exception.PorcentajeInvalidoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;

/**
 * Entidad de dominio que representa un beneficiario de una poliza.
 */
@Entity
public class Beneficiario {

    private static final int PORCENTAJE_MINIMO = 1;
    private static final int PORCENTAJE_MAXIMO = 100;
    private static final int EDAD_MAXIMA_ANOS = 100;
    private static final ZoneId ZONA_CDMX = ZoneId.of("America/Mexico_City");
    private static final char I_ACENTO = (char) 0xED;
    private static final char N_TILDE = (char) 0xF1;

    @EmbeddedId
    private BeneficiarioId id;

    private int porcentaje;

    protected Beneficiario() {
    }

    public Beneficiario(BeneficiarioId id, int porcentaje) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del beneficiario es obligatorio.");
        }
        validarPorcentaje(porcentaje);
        this.id = id;
        this.porcentaje = porcentaje;
    }

    public Beneficiario(
            String nombres,
            String primerApellido,
            String segundoApellido,
            LocalDate fechaNacimiento,
            UUID clavePoliza,
            int porcentaje) {
        this(
                new BeneficiarioId(
                        nombres, primerApellido, segundoApellido, fechaNacimiento, clavePoliza),
                porcentaje);
    }

    private void validarPorcentaje(int valorPorcentaje) {
        if (valorPorcentaje < PORCENTAJE_MINIMO || valorPorcentaje > PORCENTAJE_MAXIMO) {
            throw new PorcentajeInvalidoException(valorPorcentaje);
        }
    }

    public BeneficiarioId getId() {
        return id;
    }

    public void setId(BeneficiarioId id) {
        this.id = id;
    }

    public String getNombres() {
        return id != null ? id.getNombres() : null;
    }

    public String getPrimerApellido() {
        return id != null ? id.getPrimerApellido() : null;
    }

    public String getSegundoApellido() {
        return id != null ? id.getSegundoApellido() : null;
    }

    public LocalDate getFechaNacimiento() {
        return id != null ? id.getFechaNacimiento() : null;
    }

    public UUID getClavePoliza() {
        return id != null ? id.getClavePoliza() : null;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(int porcentaje) {
        validarPorcentaje(porcentaje);
        this.porcentaje = porcentaje;
    }

    /**
     * Valida que la fecha de nacimiento del beneficiario no sea futura ni
     * supere los 100 anos de antiguedad (si se permiten menores de edad).
     *
     * @param fechaNacimiento fecha de nacimiento a validar
     */
    public static void validarFechaNacimientoParaRegistro(LocalDate fechaNacimiento) {
        validarFechaNacimientoParaRegistro(fechaNacimiento, LocalDate.now(ZONA_CDMX));
    }

    /**
     * Valida la fecha de nacimiento del beneficiario respecto a una fecha de
     * referencia (sin fechas futuras y antiguedad maxima de 100 anos).
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
                    "La fecha de nacimiento del beneficiario no puede ser una fecha futura.");
        }
        LocalDate fechaMinima = hoy.minusYears(EDAD_MAXIMA_ANOS);
        if (fechaNacimiento.isBefore(fechaMinima)) {
            throw new ReglaNegocioException(
                    "La fecha de nacimiento del beneficiario no puede superar los "
                    + EDAD_MAXIMA_ANOS + " a" + N_TILDE + "os de antiguedad (fecha m"
                    + I_ACENTO + "nima permitida: " + fechaMinima + ").");
        }
    }

    /**
     * Calcula la fecha minima permitida para registrar un beneficiario (hoy
     * menos 100 anos en zona CDMX).
     *
     * @return fecha minima permitida
     */
    public static LocalDate fechaMinimaRegistro() {
        return LocalDate.now(ZONA_CDMX).minusYears(EDAD_MAXIMA_ANOS);
    }

    /**
     * Calcula la fecha maxima permitida para registrar un beneficiario (hoy en
     * zona CDMX, permitiendo menores de edad pero no fechas futuras).
     *
     * @return fecha maxima permitida
     */
    public static LocalDate fechaMaximaRegistro() {
        return LocalDate.now(ZONA_CDMX);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Beneficiario other)) {
            return false;
        }
        return porcentaje == other.porcentaje && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, porcentaje);
    }
}
