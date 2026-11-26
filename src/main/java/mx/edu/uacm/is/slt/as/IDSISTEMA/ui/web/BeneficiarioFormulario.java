package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;

/**
 * Objeto de respaldo para el formulario Thymeleaf de alta y modificacion de
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

    private String nombresOriginales;

    private String primerApellidoOriginal;

    private String segundoApellidoOriginal;

    private String fechaNacimientoOriginal;

    private String porcentajeOriginal;

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

    /**
     * Crea un formulario precargado con los datos actuales de un beneficiario
     * para su modificacion, preservando su llave compuesta original.
     *
     * @param beneficiario beneficiario existente a editar
     * @return formulario precargado para edicion
     */
    public static BeneficiarioFormulario desde(Beneficiario beneficiario) {
        BeneficiarioFormulario form = new BeneficiarioFormulario();
        String clave = beneficiario.getClavePoliza().toString();
        String fecha = beneficiario.getFechaNacimiento().toString();
        String segundo = beneficiario.getSegundoApellido() != null
                ? beneficiario.getSegundoApellido()
                : "";
        String porcentajeTexto = String.valueOf(beneficiario.getPorcentaje());

        form.setClavePoliza(clave);
        form.setNombres(beneficiario.getNombres());
        form.setPrimerApellido(beneficiario.getPrimerApellido());
        form.setSegundoApellido(segundo);
        form.setFechaNacimiento(fecha);
        form.setPorcentaje(porcentajeTexto);

        form.setNombresOriginales(beneficiario.getNombres());
        form.setPrimerApellidoOriginal(beneficiario.getPrimerApellido());
        form.setSegundoApellidoOriginal(segundo);
        form.setFechaNacimientoOriginal(fecha);
        form.setPorcentajeOriginal(porcentajeTexto);
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

    public String getNombresOriginales() {
        return nombresOriginales;
    }

    public void setNombresOriginales(String nombresOriginales) {
        this.nombresOriginales = nombresOriginales;
    }

    public String getPrimerApellidoOriginal() {
        return primerApellidoOriginal;
    }

    public void setPrimerApellidoOriginal(String primerApellidoOriginal) {
        this.primerApellidoOriginal = primerApellidoOriginal;
    }

    public String getSegundoApellidoOriginal() {
        return segundoApellidoOriginal;
    }

    public void setSegundoApellidoOriginal(String segundoApellidoOriginal) {
        this.segundoApellidoOriginal = segundoApellidoOriginal;
    }

    public String getFechaNacimientoOriginal() {
        return fechaNacimientoOriginal;
    }

    public void setFechaNacimientoOriginal(String fechaNacimientoOriginal) {
        this.fechaNacimientoOriginal = fechaNacimientoOriginal;
    }

    public String getPorcentajeOriginal() {
        return porcentajeOriginal;
    }

    public void setPorcentajeOriginal(String porcentajeOriginal) {
        this.porcentajeOriginal = porcentajeOriginal;
    }
}
