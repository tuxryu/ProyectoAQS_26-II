package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;

@Entity
@Table(name = "beneficiario_poliza")
public class Beneficiario {

    @EmbeddedId
    private BeneficiarioId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("clavePoliza")
    @JoinColumn(name = "clave_poliza", nullable = false)
    private Poliza poliza;

    @NotBlank
    @Column(name = "segundo_apellido", length = 128)
    private String segundoApellido;

    @NotNull
    @Min(1)
    @Max(100)
    @Column(name = "porcentaje", nullable = false)
    private Integer porcentaje;

    public Beneficiario() {
    }

    public Beneficiario(
            String nombres,
            String primerApellido,
            String segundoApellido,
            LocalDate fechaNacimiento,
            Poliza poliza,
            Integer porcentaje) {

        this.id = new BeneficiarioId(
                poliza != null ? poliza.getClave() : null,
                fechaNacimiento,
                nombres,
                primerApellido);

        this.poliza = poliza;
        this.segundoApellido = segundoApellido;
        this.porcentaje = porcentaje;
    }

    public BeneficiarioId getId() {
        return id;
    }

    public void setId(BeneficiarioId id) {
        this.id = id;
    }

    public Poliza getPoliza() {
        return poliza;
    }

    public void setPoliza(Poliza poliza) {
        this.poliza = poliza;

        if (this.id != null && poliza != null) {
            this.id.setClavePoliza(poliza.getClave());
        }
    }

    public String getNombres() {
        return id != null ? id.getNombres() : null;
    }

    public String getPrimerApellido() {
        return id != null ? id.getPrimerApellido() : null;
    }

    public LocalDate getFechaNacimiento() {
        return id != null ? id.getFechaNacimiento() : null;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public Integer getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Integer porcentaje) {
        this.porcentaje = porcentaje;
    }
}