package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "clientes")
public class Cliente {

    @EmbeddedId
    private ClienteId id;

    @Column(name = "segundo_apellido")
    private String segundoApellido;

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    // JPA requiere un constructor vacío
    protected Cliente() {}

    public Cliente(String curp, String nombres, String primerApellido, String segundoApellido, String direccion, LocalDate fechaNacimiento) {
        validarCamposObligatorios(curp, nombres, primerApellido, direccion, fechaNacimiento);
        validarCurp(curp);

        this.id = new ClienteId(curp, nombres, primerApellido);
        this.segundoApellido = segundoApellido;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
    }

    private void validarCamposObligatorios(String curp, String nombres, String primerApellido, String direccion, LocalDate fechaNacimiento) {
        if (curp == null || curp.trim().isEmpty()) {
            throw new IllegalArgumentException("El CURP no puede ser nulo ni vacío.");
        }
        if (nombres == null || nombres.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo ni vacío.");
        }
        if (primerApellido == null || primerApellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El primer apellido no puede ser nulo ni vacío.");
        }
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección no puede ser nula ni vacía.");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula.");
        }
    }

    private void validarCurp(String curp) {
        if (curp.length() != 18) {
            throw new IllegalArgumentException("El CURP debe tener exactamente 18 caracteres.");
        }
    }

    public ClienteId getId() { return id; }
    public String getCurp() { return id != null ? id.getCurp() : null; }
    public String getNombres() { return id != null ? id.getNombres() : null; }
    public String getPrimerApellido() { return id != null ? id.getPrimerApellido() : null; }
    public String getSegundoApellido() { return segundoApellido; }
    public String getDireccion() { return direccion; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
}