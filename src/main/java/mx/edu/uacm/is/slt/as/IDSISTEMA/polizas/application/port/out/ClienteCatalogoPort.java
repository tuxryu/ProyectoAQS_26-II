package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out;

import java.util.List;

/**
 * Puerto de salida del modulo de polizas para obtener el catalogo de clientes
 * disponibles. Se usa exclusivamente para poblar selectores en la interfaz
 * web (alta y modificacion de polizas).
 */
public interface ClienteCatalogoPort {

    /**
     * Lista todos los clientes registrados en el sistema.
     *
     * @return lista inmutable de clientes; vacia si no hay registros.
     */
    List<?> listarClientes();
}