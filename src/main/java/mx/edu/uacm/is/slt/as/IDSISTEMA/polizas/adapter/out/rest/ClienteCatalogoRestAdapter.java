package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.out.rest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultaClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.FiltroCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.ClienteCatalogoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Adaptador inter-modulo en memoria que implementa {@link ClienteCatalogoPort}
 * invocando {@link ConsultaClienteUseCase} del modulo de clientes sin realizar
 * llamadas HTTP internas.
 */
@Component
public class ClienteCatalogoRestAdapter implements ClienteCatalogoPort {

    private static final int TAMANO_CATALOGO = 100;

    private final ConsultaClienteUseCase consultaClienteUseCase;

    /**
     * Crea el adaptador con el caso de uso de consulta de clientes.
     *
     * @param consultaClienteUseCase Optional con el caso de uso de clientes
     */
    @Autowired
    public ClienteCatalogoRestAdapter(
            Optional<ConsultaClienteUseCase> consultaClienteUseCase) {
        this.consultaClienteUseCase = Objects.requireNonNull(
                        consultaClienteUseCase,
                        "El contenedor Optional de ConsultaClienteUseCase no puede ser nulo.")
                .orElse(null);
    }

    @Override
    public List<Cliente> listarClientes() {
        if (consultaClienteUseCase == null) {
            return List.of(); // Si no está, devolvemos vacío en vez de explotar
        }
        try {
            Pagina<Cliente> pagina = consultaClienteUseCase.consultar(
                    new FiltroCliente.Todos(), 0, TAMANO_CATALOGO);
            return pagina.contenido();
        } catch (Exception ex) {
            // Si el Sistema Dueño falla, no tumbamos la vista, solo devolvemos vacío
            // (Opcionalmente, aquí podrías loggear el error)
            return List.of();
        }
    }
}