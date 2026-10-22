package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.out;

import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import java.util.List;

public interface PolizaServicioPort {
    Poliza obtenerPolizaPorClave(String clave);
    List<Poliza> obtenerPolizasPorCliente(String idCliente);
}
