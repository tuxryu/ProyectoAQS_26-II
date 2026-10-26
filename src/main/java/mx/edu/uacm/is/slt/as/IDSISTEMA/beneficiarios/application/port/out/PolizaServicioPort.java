package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out;

import java.util.UUID;

public interface PolizaServicioPort {

    boolean existePolizaPorClave(UUID clave);

}
