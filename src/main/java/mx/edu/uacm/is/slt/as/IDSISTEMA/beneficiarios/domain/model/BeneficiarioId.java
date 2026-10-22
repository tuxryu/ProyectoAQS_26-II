package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public class BeneficiarioId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID clavePoliza;
    private LocalDate fechaNacimiento;
    private String nombres;
    private String primerApellido;

    public BeneficiarioId() {
    }

    public BeneficiarioId(
            UUID clavePoliza,
            LocalDate fechaNacimiento,
            String nombres,
            String primerApellido) {
        this.clavePoliza = clavePoliza;
        this.fechaNacimiento = fechaNacimiento;
        this.nombres = nombres;
        this.primerApellido = primerApellido;
    }

    public UUID getClavePoliza() {
        return clavePoliza;
    }

    public void setClavePoliza(UUID clavePoliza) {
        this.clavePoliza = clavePoliza;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
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

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof BeneficiarioId otro)) {
            return false;
        }

        return Objects.equals(clavePoliza, otro.clavePoliza)
                && Objects.equals(fechaNacimiento, otro.fechaNacimiento)
                && Objects.equals(nombres, otro.nombres)
                && Objects.equals(primerApellido, otro.primerApellido);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                clavePoliza,
                fechaNacimiento,
                nombres,
                primerApellido);
    }
}