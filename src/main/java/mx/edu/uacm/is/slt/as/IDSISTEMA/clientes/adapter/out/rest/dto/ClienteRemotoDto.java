package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest.dto;

import java.time.LocalDate;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http.FechasSistemaDueno;

/**
 * DTO en camelCase para mapear la respuesta JSON del Sistema Dueno en
 * operaciones de clientes.
 */
public class ClienteRemotoDto {

    private String curp;
    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String direccion;
    private String fechaNacimiento;

    public ClienteRemotoDto() {
    }

    public ClienteRemotoDto(
            String curp,
            String nombres,
            String primerApellido,
            String segundoApellido,
            String direccion,
            String fechaNacimiento) {
        this.curp = curp;
        this.nombres = nombres;
        this.primerApellido = primerApellido;
        this.segundoApellido = normalizarSegundoApellido(segundoApellido);
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
    }

    private String normalizarSegundoApellido(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor;
    }

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
        return normalizarSegundoApellido(segundoApellido);
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = normalizarSegundoApellido(segundoApellido);
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Convierte el timestamp de fechaNacimiento a LocalDate en zona CDMX.
     *
     * @return fecha de nacimiento como LocalDate
     */
    public LocalDate obtenerFechaNacimientoLocal() {
        return FechasSistemaDueno.aLocalDate(fechaNacimiento);
    }
}
