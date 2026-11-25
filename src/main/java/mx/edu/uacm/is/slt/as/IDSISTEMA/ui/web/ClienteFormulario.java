package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;

/**
 * Objeto de respaldo para los formularios Thymeleaf de alta y modificacion de
 * clientes. Conserva la fecha como texto ISO (AAAA-MM-DD) para manejar errores
 * de formato con alertas amigables dentro del controlador de vistas.
 */
public class ClienteFormulario {

    private String curp;

    private String nombres;

    private String primerApellido;

    private String segundoApellido;

    private String direccion;

    private String fechaNacimiento;

    /**
     * Crea un formulario vacio para el alta de un cliente.
     */
    public ClienteFormulario() {
    }

    /**
     * Construye un formulario precargado con los datos actuales de un cliente.
     *
     * @param cliente entidad de dominio del cliente
     * @return formulario listo para edicion en Thymeleaf
     */
    public static ClienteFormulario desde(Cliente cliente) {
        ClienteFormulario form = new ClienteFormulario();
        form.setCurp(cliente.getCurp());
        form.setNombres(cliente.getNombres());
        form.setPrimerApellido(cliente.getPrimerApellido());
        form.setSegundoApellido(cliente.getSegundoApellido());
        form.setDireccion(cliente.getDireccion());
        if (cliente.getFechaNacimiento() != null) {
            form.setFechaNacimiento(cliente.getFechaNacimiento().toString());
        }
        return form;
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

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}
