package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

/**
 * Objeto de respaldo para el formulario Thymeleaf de alta vinculada de
 * beneficiarios. Conserva todos los campos como texto para manejar errores de
 * formato (UUID, fecha y porcentaje) con alertas amigables en el controlador.
 */
public class BeneficiarioFormulario {

    private String clavePoliza;

    private String nombres;

    private String primerApellido;

    private String segundoApellido;

    private String fechaNacimiento;

    private String porcentaje;

    /**
     * Crea un formulario vacio para el alta de beneficiario.
     */
    public BeneficiarioFormulario() {
    }

    /**
     * Crea un formulario precargado con la clave de la poliza seleccionada.
     *
     * @param clavePoliza clave UUID de la poliza en texto
     * @return formulario inicializado con la poliza vinculada
     */
    public static BeneficiarioFormulario paraPoliza(String clavePoliza) {
        BeneficiarioFormulario form = new BeneficiarioFormulario();
        form.setClavePoliza(clavePoliza);
        return form;
    }

    public String getClavePoliza() {
        return clavePoliza;
    }

    public void setClavePoliza(String clavePoliza) {
        this.clavePoliza = clavePoliza;
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

    public String getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(String porcentaje) {
        this.porcentaje = porcentaje;
    }
}
