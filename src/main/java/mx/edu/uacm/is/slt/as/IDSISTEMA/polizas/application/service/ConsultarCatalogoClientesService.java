package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.service;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultarCatalogoClientesUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.ClienteCatalogoPort;

@Service
public class ConsultarCatalogoClientesService implements ConsultarCatalogoClientesUseCase {

    private final ClienteCatalogoPort clienteCatalogoPort;

    public ConsultarCatalogoClientesService(ClienteCatalogoPort clienteCatalogoPort) {
        this.clienteCatalogoPort = Objects.requireNonNull(clienteCatalogoPort);
    }

    @Override
    public List<?> obtenerCatalogo() {
        return clienteCatalogoPort.listarClientes();
    }
}