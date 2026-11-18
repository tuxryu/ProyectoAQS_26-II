package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.FiltroCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.out.ClienteRemotoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.service.ClienteService;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.service.ConsultarClientePorCurpService;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.exception.ClienteNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de {@link ClienteService} y
 * {@link ConsultarClientePorCurpService} con un puerto falso en memoria (sin
 * Spring).
 */
class ClienteServiceTest {

    private static final String CURP_A = "AAAA800101HDFABC01";

    private static final String CURP_B = "BROE550101HDFABC00";

    private static final String CURP_INEXISTENTE = "XXXX000101HDFNNN00";

    private PuertoClienteFalso puertoFalso;
    private ClienteService service;
    private ConsultarClientePorCurpService consultarPorCurpService;

    @BeforeEach
    void configurar() {
        puertoFalso = new PuertoClienteFalso();
        service = new ClienteService(puertoFalso);
        consultarPorCurpService = new ConsultarClientePorCurpService(puertoFalso);
    }

    @Test
    void debeConsultarClientePorCurpYLanzar404CuandoNoExiste() {
        Cliente encontrado = service.consultarPorCurp(CURP_B);
        assertEquals("Emmett", encontrado.getNombres());

        assertThrows(
                ClienteNoEncontradoException.class,
                () -> service.consultarPorCurp(CURP_INEXISTENTE));

        assertTrue(consultarPorCurpService.consultarPorCurp(CURP_B).isPresent());
        assertTrue(consultarPorCurpService.consultarPorCurp(CURP_INEXISTENTE).isEmpty());
    }

    @Test
    void debeListarClientesPaginadosOrdenadosPorCurpYFiltradosPorNombre() {
        Pagina<Cliente> pagina = service.consultar(new FiltroCliente.Todos(), 0, 10);
        assertEquals(2, pagina.contenido().size());
        assertEquals(CURP_A, pagina.contenido().get(0).getCurp());
        assertEquals(CURP_B, pagina.contenido().get(1).getCurp());

        Pagina<Cliente> porNombre = service.consultar(
                new FiltroCliente.PorNombre("Emmett", "Brown", null), 0, 10);
        assertEquals(1, porNombre.contenido().size());
        assertEquals(CURP_B, porNombre.contenido().get(0).getCurp());
    }

    @Test
    void debeRegistrarNuevoClienteYRechazarCurpDuplicadaCon409() {
        Cliente nuevo = new Cliente(
                CURP_INEXISTENTE,
                "Marty",
                "McFly",
                null,
                "Lyon Estates 9303",
                LocalDate.of(1968, 6, 12));

        Cliente registrado = service.registrarCliente(nuevo);
        assertEquals("Marty", registrado.getNombres());

        assertThrows(
                ConflictoDatosException.class,
                () -> service.registrarCliente(nuevo));
    }

    @Test
    void debeActualizarClienteExistenteYLanzar404SiNoExiste() {
        Cliente actualizado = new Cliente(
                CURP_B,
                "Emmett",
                "Brown",
                "Lathrop",
                "Nueva Direccion 1885",
                LocalDate.of(1955, 1, 1));

        Cliente resultado = service.actualizarCliente(actualizado);
        assertEquals("Nueva Direccion 1885", resultado.getDireccion());

        Cliente inexistente = new Cliente(
                CURP_INEXISTENTE,
                "Nadie",
                "Ninguno",
                null,
                "Sin calle 0",
                LocalDate.of(1990, 1, 1));
        assertThrows(
                ClienteNoEncontradoException.class,
                () -> service.actualizarCliente(inexistente));
    }

    @Test
    void debeRechazarParametrosDePaginacionInvalidos() {
        assertThrows(
                SolicitudInvalidaException.class,
                () -> service.consultar(new FiltroCliente.Todos(), -1, 10));
        assertThrows(
                SolicitudInvalidaException.class,
                () -> service.consultar(new FiltroCliente.Todos(), 0, 0));
    }

    private static final class PuertoClienteFalso implements ClienteRemotoPort {

        private final List<Cliente> almacen = new ArrayList<>(List.of(
                new Cliente(
                        CURP_B,
                        "Emmett",
                        "Brown",
                        "Lathrop",
                        "Av. Universidad 123",
                        LocalDate.of(1955, 1, 1)),
                new Cliente(
                        CURP_A,
                        "Clara",
                        "Clayton",
                        "Smith",
                        "Hill Valley 10",
                        LocalDate.of(1960, 5, 15))));

        @Override
        public Optional<Cliente> buscarPorCurp(String curp) {
            return almacen.stream()
                    .filter(c -> Objects.equals(c.getCurp(), curp))
                    .findFirst();
        }

        @Override
        public List<Cliente> listarTodos() {
            return List.copyOf(almacen);
        }

        @Override
        public List<Cliente> buscarPorNombre(
                String nombres, String primerApellido, String segundoApellido) {
            return almacen.stream()
                    .filter(c -> Objects.equals(c.getNombres(), nombres)
                            && Objects.equals(c.getPrimerApellido(), primerApellido))
                    .toList();
        }

        @Override
        public Cliente guardar(Cliente cliente) {
            almacen.add(cliente);
            return cliente;
        }

        @Override
        public Cliente actualizar(Cliente cliente) {
            almacen.removeIf(c -> Objects.equals(c.getCurp(), cliente.getCurp()));
            almacen.add(cliente);
            return cliente;
        }
    }
}
