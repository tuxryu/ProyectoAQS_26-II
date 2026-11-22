package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;

/**
 * Objeto de respaldo para los formularios Thymeleaf de alta y modificacion.
 * de polizas. Conserva tipo y monto como texto para manejar errores de
 * formato con alertas amigables dentro del controlador de vistas.
 */
public class PolizaFormulario {

    private String clave;
    private String tipo;
    private String monto;
    private String descripcion;
    private String curpCliente;

    public PolizaFormulario() {

    }

    public static PolizaFormulario desde(Poliza poliza) {
        PolizaFormulario form = new PolizaFormulario();
        form.setClave(poliza.getClave() != null ? poliza.getClave().toString() : null);
        form.setTipo(String.valueOf(poliza.getTipo()));
        form.setMonto(String.valueOf(poliza.getMonto()));
        form.setDescripcion(poliza.getDescripcion());
        form.setCurpCliente(poliza.getCurpCliente());
        return form;
    }

    public String getClave()
    { return clave; }

    public void setClave(String clave)
    { this.clave = clave; }

    public String getTipo()
    { return tipo; }

    public void setTipo(String tipo)
    { this.tipo = tipo; }

    public String getMonto()
    { return monto; }

    public void setMonto(String monto)
    { this.monto = monto; }

    public String getDescripcion()
    { return descripcion; }

    public void setDescripcion(String descripcion)
    { this.descripcion = descripcion; }

    public String getCurpCliente()
    { return curpCliente; }

    public void setCurpCliente(String curpCliente) { this.curpCliente = curpCliente; }
}