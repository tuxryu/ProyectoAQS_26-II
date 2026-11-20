package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;

/**
 * Objeto de respaldo para los formularios Thymeleaf de alta y modificacion de
 * beneficiarios. Conserva la fecha y la clave de poliza como texto para manejar
 * errores de formato con alertas amigables dentro del controlador de vistas.
 */
public class BeneficiarioFormulario {

    private String nombres;

    private String primerApellido;

    private String segundoApellido;

    private String fechaNacimiento;

    private String clavePoliza;

    private Integer porcentaje;

    public BeneficiarioFormulario() {
    }

    /**
     * Construye un formulario precargado con los datos actuales de un beneficiario.
     *
     * @param beneficiario entidad de dominio del beneficiario
     * @return formulario listo para edicion en Thymeleaf
     */
    public static BeneficiarioFormulario desde(Beneficiario beneficiario) {
        BeneficiarioFormulario form = new BeneficiarioFormulario();
        form.setNombres(beneficiario.getNombres());
        form.setPrimerApellido(beneficiario.getPrimerApellido());
        form.setSegundoApellido(beneficiario.getSegundoApellido());
        if (beneficiario.getFechaNacimiento() != null) {
            form.setFechaNacimiento(beneficiario.getFechaNacimiento().toString());
        }
        if (beneficiario.getClavePoliza() != null) {
            form.setClavePoliza(beneficiario.getClavePoliza().toString());
        }
        form.setPorcentaje(beneficiario.getPorcentaje());
        return form;
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

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getClavePoliza() {
        return clavePoliza;
    }

    public void setClavePoliza(String clavePoliza) {
        this.clavePoliza = clavePoliza;
    }

    public Integer getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Integer porcentaje) {
        this.porcentaje = porcentaje;
    }
}