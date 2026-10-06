package mx.uacm.aqs.polizas.application.port.out;

import java.util.List;
import mx.uacm.aqs.polizas.domain.model.Poliza;

public interface PolizaRemotaPort {

    Poliza guardar(Poliza poliza);

    List<Poliza> consultarPorCliente(String curpCliente);
}
