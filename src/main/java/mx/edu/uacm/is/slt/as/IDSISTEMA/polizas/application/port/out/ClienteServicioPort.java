package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out;

/**
 * Puerto de salida para verificar la existencia del cliente titular desde el
 * modulo de polizas.
 */
public interface ClienteServicioPort {

    boolean existeClientePorCurp(String curp);
}
