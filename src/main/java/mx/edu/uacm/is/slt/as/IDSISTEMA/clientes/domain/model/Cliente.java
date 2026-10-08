package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model;

import java.time.LocalDate;

public class Cliente {

    private String curp; // PK, 18 caracteres
    private String nombres;
    private String primerApellido;
    private String segundoApellido; // Opcional
    private String direccion;
    private LocalDate fechaNacimiento;

    public Cliente(String curp, String nombres, String primerApellido, String segundoApellido, String direccion, LocalDate fechaNacimiento) {
        validarCamposObligatorios(curp, nombres, primerApellido, direccion, fechaNacimiento);
        validarCurp(curp);

        this.curp = curp;
        this.nombres = nombres;
        this.primerApellido = primerApellido;
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

    public String getCurp() { return curp; }
    public String getNombres() { return nombres; }
    public String getPrimerApellido() { return primerApellido; }
    public String getSegundoApellido() { return segundoApellido; }
    public String getDireccion() { return direccion; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
}