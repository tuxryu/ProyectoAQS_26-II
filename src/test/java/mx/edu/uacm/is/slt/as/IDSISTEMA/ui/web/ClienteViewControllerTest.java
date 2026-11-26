package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ActualizarClienteUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultaClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.FiltroCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.RegistrarClienteUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.exception.ClienteNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas WebMvc para {@link ClienteViewController}: verifica listado paginado,
 * consulta por CURP, alta, modificacion y alertas amigables ante 404, 409, 422,
 * 502 y 503.
 */
@WebMvcTest(ClienteViewController.class)
class ClienteViewControllerTest {

    private static final String CURP_VALIDA = "BROE550101HDFABC00";

    private static final String CURP_MINUSCULA = "broe550101hdfabc00";

    private static final String CURP_INEXISTENTE = "XXXX000101HDFNNN00";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaClienteUseCase consultaClienteUseCase;

    @MockitoBean
    private RegistrarClienteUseCasePort registrarClienteUseCasePort;

    @MockitoBean
    private ActualizarClienteUseCasePort actualizarClienteUseCasePort;

    private Cliente crearCliente(String curp) {
        return new Cliente(
                curp,
                "Emmett",
                "Brown",
                "Lathrop",
                "Av. Universidad 123",
                LocalDate.of(1955, 1, 1));
    }

    @Test
    void debeMostrarListadoPaginadoDeClientes() throws Exception {
        Cliente cliente = crearCliente(CURP_MINUSCULA);
        when(consultaClienteUseCase.consultar(any(FiltroCliente.Todos.class), eq(0), eq(10)))
                .thenReturn(Pagina.de(List.of(cliente), 0, 10));

        mockMvc.perform(get("/web/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/lista"))
                .andExpect(model().attributeExists("paginaClientes"))
                .andExpect(content().string(containsString(CURP_MINUSCULA)))
                .andExpect(content().string(containsString("Emmett")));
    }

    @Test
    void debeConsultarClientePorCurpRespetandoMinusculas() throws Exception {
        Cliente cliente = crearCliente(CURP_MINUSCULA);
        when(consultaClienteUseCase.consultarPorCurp(CURP_MINUSCULA))
                .thenReturn(cliente);

        mockMvc.perform(get("/web/clientes/consultar").param("curp", CURP_MINUSCULA))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/detalle"))
                .andExpect(model().attribute("cliente", cliente))
                .andExpect(content().string(containsString(CURP_MINUSCULA)));
    }

    @Test
    void debeRegistrarClienteYRedirigirAlDetalleConAlertaDeExito() throws Exception {
        Cliente creado = crearCliente(CURP_VALIDA);
        when(registrarClienteUseCasePort.registrarCliente(any(Cliente.class)))
                .thenReturn(creado);

        mockMvc.perform(post("/web/clientes/nuevo")
                .param("curp", CURP_VALIDA)
                .param("nombres", "Emmett")
                .param("primerApellido", "Brown")
                .param("segundoApellido", "Lathrop")
                .param("direccion", "Av. Universidad 123")
                .param("fechaNacimiento", "1955-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/clientes/consultar?curp=" + CURP_VALIDA))
                .andExpect(flash().attributeExists("alerta"));
    }

    @Test
    void debeCargarYActualizarClienteExistente() throws Exception {
        Cliente existente = crearCliente(CURP_MINUSCULA);
        when(consultaClienteUseCase.consultarPorCurp(CURP_MINUSCULA))
                .thenReturn(existente);
        when(actualizarClienteUseCasePort.actualizarCliente(any(Cliente.class)))
                .thenReturn(existente);

        mockMvc.perform(get("/web/clientes/{curp}/editar", CURP_MINUSCULA))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/formulario"))
                .andExpect(model().attribute("modoEdicion", true))
                .andExpect(content().string(containsString(CURP_MINUSCULA)));

        mockMvc.perform(post("/web/clientes/{curp}/editar", CURP_MINUSCULA)
                .param("nombres", "Emmett")
                .param("primerApellido", "Brown")
                .param("segundoApellido", "")
                .param("direccion", "Calle Pino 456")
                .param("fechaNacimiento", "1955-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/clientes/consultar?curp=" + CURP_MINUSCULA))
                .andExpect(flash().attributeExists("alerta"));
    }

    @Test
    void debeMostrarAlerta404CuandoNoExisteElCliente() throws Exception {
        when(consultaClienteUseCase.consultarPorCurp(CURP_INEXISTENTE))
                .thenThrow(new ClienteNoEncontradoException(CURP_INEXISTENTE));

        mockMvc.perform(get("/web/clientes/consultar").param("curp", CURP_INEXISTENTE))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/detalle"))
                .andExpect(content().string(containsString("HTTP 404")))
                .andExpect(content().string(containsString(CURP_INEXISTENTE)));
    }

    @Test
    void debeMostrarAlerta409CuandoCurpYaExisteEnAlta() throws Exception {
        when(registrarClienteUseCasePort.registrarCliente(any(Cliente.class)))
                .thenThrow(new ConflictoDatosException(
                        "El cliente con CURP " + CURP_VALIDA + " ya se encuentra registrado."));

        mockMvc.perform(post("/web/clientes/nuevo")
                .param("curp", CURP_VALIDA)
                .param("nombres", "Emmett")
                .param("primerApellido", "Brown")
                .param("direccion", "Av. Universidad 123")
                .param("fechaNacimiento", "1955-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/formulario"))
                .andExpect(content().string(containsString("HTTP 409")))
                .andExpect(content().string(containsString("Emmett")));
    }

    @Test
    void debeMostrarAlerta422CuandoDatosDeAltaSonInvalidos() throws Exception {
        mockMvc.perform(post("/web/clientes/nuevo")
                .param("curp", CURP_MINUSCULA)
                .param("nombres", "Emmett")
                .param("primerApellido", "Brown")
                .param("direccion", "Av. Universidad 123")
                .param("fechaNacimiento", "1955-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/formulario"))
                .andExpect(content().string(containsString("HTTP 422")));

        mockMvc.perform(get("/web/clientes/consultar").param("curp", "ABC/123"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/detalle"))
                .andExpect(content().string(containsString("HTTP 422")));

        mockMvc.perform(post("/web/clientes/nuevo")
                .param("curp", CURP_VALIDA)
                .param("nombres", "Emmett")
                .param("primerApellido", "Brown")
                .param("direccion", "Av. Universidad 123")
                .param("fechaNacimiento", LocalDate.now().plusDays(10).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/formulario"))
                .andExpect(content().string(containsString("HTTP 422")));

        mockMvc.perform(post("/web/clientes/nuevo")
                .param("curp", CURP_VALIDA)
                .param("nombres", "Emmett")
                .param("primerApellido", "Brown")
                .param("direccion", "Av. Universidad 123")
                .param("fechaNacimiento", "1825-05-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/formulario"))
                .andExpect(content().string(containsString("HTTP 422")));

        mockMvc.perform(post("/web/clientes/nuevo")
                .param("curp", CURP_VALIDA)
                .param("nombres", "Emmett")
                .param("primerApellido", "Brown")
                .param("direccion", "Av. Universidad 123")
                .param("fechaNacimiento", LocalDate.now().minusYears(15).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/formulario"))
                .andExpect(content().string(containsString("HTTP 422")));
    }

    @Test
    void debeMostrarAlerta502SinFiltrarDetallesInternos() throws Exception {
        when(registrarClienteUseCasePort.registrarCliente(any(Cliente.class)))
                .thenThrow(new SistemaDuenoRechazoException(
                        "HTTP 500 en http://nachintoch.mx:8080/cliente"));

        mockMvc.perform(post("/web/clientes/nuevo")
                .param("curp", CURP_VALIDA)
                .param("nombres", "Emmett")
                .param("primerApellido", "Brown")
                .param("direccion", "Av. Universidad 123")
                .param("fechaNacimiento", "1955-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/formulario"))
                .andExpect(content().string(containsString("HTTP 502")))
                .andExpect(content().string(not(containsString("nachintoch.mx"))));
    }

    @Test
    void debeMostrarAlerta503CuandoSistemaDuenoNoEstaDisponible() throws Exception {
        when(consultaClienteUseCase.consultar(any(FiltroCliente.class), eq(0), eq(10)))
                .thenThrow(new SistemaDuenoNoDisponibleException(
                        "Timeout al conectar con http://nachintoch.mx:8080"));

        mockMvc.perform(get("/web/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/lista"))
                .andExpect(content().string(containsString("HTTP 503")))
                .andExpect(content().string(not(containsString("nachintoch.mx"))));
    }
}
