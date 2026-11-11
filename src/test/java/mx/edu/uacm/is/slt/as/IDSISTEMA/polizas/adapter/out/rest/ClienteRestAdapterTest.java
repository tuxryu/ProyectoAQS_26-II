package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.out.rest;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultarClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para {@link ClienteRestAdapter}.
 */
class ClienteRestAdapterTest {

    @Test
    void debeRetornarTrueCuandoClienteExisteEnMemoria() {
        Cliente cliente = new Cliente(
                "BROE550101HDFABC00",
                "Emmett",
                "Brown",
                "Lathrop",
                "Av. Universidad 123",
                LocalDate.of(1955, 1, 1));
        ConsultarClienteUseCase useCase = curp -> Optional.of(cliente);
        ClienteRestAdapter adapter = new ClienteRestAdapter(useCase);

        assertTrue(adapter.existeClientePorCurp("BROE550101HDFABC00"));
    }

    @Test
    void debeRetornarFalseCuandoClienteNoExisteODevuelveVacio() {
        ConsultarClienteUseCase useCase = curp -> Optional.empty();
        ClienteRestAdapter adapter = new ClienteRestAdapter(useCase);

        assertFalse(adapter.existeClientePorCurp("BROE550101HDFABC00"));
        assertFalse(adapter.existeClientePorCurp(null));
        assertFalse(adapter.existeClientePorCurp("   "));
    }

    @Test
    void debeRetornarFalseCuandoCasoDeUsoLanzaRecursoNoEncontradoException() {
        ConsultarClienteUseCase useCase = curp -> {
            throw new RecursoNoEncontradoException("No existe el cliente con CURP " + curp);
        };
        ClienteRestAdapter adapter = new ClienteRestAdapter(useCase);

        assertFalse(adapter.existeClientePorCurp("BROE550101HDFABC00"));
    }

    @Test
    void debeRechazarCasoDeUsoNuloEnConstructorDirecto() {
        assertThrows(
                NullPointerException.class,
                () -> new ClienteRestAdapter((ConsultarClienteUseCase) null));
    }
}
