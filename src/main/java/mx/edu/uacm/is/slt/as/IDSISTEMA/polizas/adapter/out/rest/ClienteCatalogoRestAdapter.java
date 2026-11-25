package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.out.rest;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultaClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.FiltroCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.ClienteCatalogoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

@Component
public class ClienteCatalogoRestAdapter implements ClienteCatalogoPort {

    private static final int TAMANO_CATALOGO = 100;
    private final ConsultaClienteUseCase consultaClienteUseCase;

    @Autowired
    public ClienteCatalogoRestAdapter(Optional<ConsultaClienteUseCase> consultaClienteUseCase) {
        this.consultaClienteUseCase = consultaClienteUseCase.orElse(null);
    }

    @Override
    public List<?> listarClientes() {
        if (consultaClienteUseCase == null) {
            return List.of();
        }
        try {
            Pagina<Cliente> pagina = consultaClienteUseCase.consultar(
                    new FiltroCliente.Todos(), 0, TAMANO_CATALOGO);
            return pagina.contenido();
        } catch (Exception ex) {
            return List.of();
        }
    }
}