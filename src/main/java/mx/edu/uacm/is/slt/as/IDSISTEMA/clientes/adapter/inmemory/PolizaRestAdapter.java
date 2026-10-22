package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.inmemory;

import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.ports.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.out.PolizaServicioPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PolizaRestAdapter implements PolizaServicioPort {

    private final ConsultaPolizaUseCase consultaPolizaUseCase;

    public PolizaRestAdapter(ConsultaPolizaUseCase consultaPolizaUseCase) {
        this.consultaPolizaUseCase = consultaPolizaUseCase;
    }

    @Override
    public Poliza obtenerPolizaPorClave(String clave) {
        return consultaPolizaUseCase.consultarPorClave(clave);
    }

    @Override
    public List<Poliza> obtenerPolizasPorCliente(String idCliente) {
        return consultaPolizaUseCase.consultarPorCliente(idCliente);
    }
}