package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

/**
 * Entidad de dominio que representa un beneficiario de una poliza.
 */
@Entity
public class Beneficiario {

    private static final int PORCENTAJE_MINIMO = 1;
    private static final int PORCENTAJE_MAXIMO = 100;

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
            throw new IllegalArgumentException("El porcentaje debe estar entre 1 y 100.");
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
