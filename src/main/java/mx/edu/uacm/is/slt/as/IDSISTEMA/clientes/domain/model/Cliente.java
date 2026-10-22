package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model;

import jakarta.persistence.Id;
import java.time.LocalDate;
import java.util.Objects;

public class Cliente {

    @Id
    private String curp;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String direccion;
    private LocalDate fechaNacimiento;

    // Constructor vacío
    public Cliente() {
    }

    // Constructor completo
    public Cliente(String curp, String nombres, String primerApellido,
                   String segundoApellido, String direccion, LocalDate fechaNacimiento) {
        this.curp = curp;
        this.nombres = nombres;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cliente cliente)) return false;
        return Objects.equals(curp, cliente.curp) &&
                Objects.equals(nombres, cliente.nombres) &&
                Objects.equals(primerApellido, cliente.primerApellido) &&
                Objects.equals(segundoApellido, cliente.segundoApellido) &&
                Objects.equals(direccion, cliente.direccion) &&
                Objects.equals(fechaNacimiento, cliente.fechaNacimiento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(curp, nombres, primerApellido, segundoApellido, direccion, fechaNacimiento);
    }

    // Getters y Setters
    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}