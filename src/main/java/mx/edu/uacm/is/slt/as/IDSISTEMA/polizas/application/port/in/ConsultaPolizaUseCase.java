package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in;

import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Puerto de entrada para las consultas de polizas (lectura por clave y
 * listados con filtro y paginacion).
 */
public interface ConsultaPolizaUseCase {

    /**
     * Consulta una poliza por su clave.
     *
     * @param clave clave UUID de la poliza
     * @return la poliza encontrada
     * @throws mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException
     *         si no existe
     */
    Poliza consultarPorClave(UUID clave);

    /**
     * Lista las polizas que cumplen el filtro, paginadas y ordenadas por clave.
     *
     * @param filtro filtro de la consulta ({@link FiltroPoliza.Todas} para no filtrar)
     * @param pagina numero de pagina, empezando en 0
     * @param tamano elementos por pagina
     * @return pagina de polizas; puede venir vacia
     */
    Pagina<Poliza> consultar(FiltroPoliza filtro, int pagina, int tamano);
}
