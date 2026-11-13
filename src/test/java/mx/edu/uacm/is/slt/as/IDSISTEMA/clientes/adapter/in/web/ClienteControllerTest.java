package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.in.web;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ActualizarClienteUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultaClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.FiltroCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.RegistrarClienteUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.exception.ClienteNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion WebMvc (@WebMvcTest) para {@link ClienteController}:
 * verifica los codigos HTTP 201, 200, 404, 409 y 422 junto con
 * GlobalExceptionHandler.
 */
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    private static final String CURP_VALIDA = "BROE550101HDFABC00";

    private static final String CURP_INEXISTENTE = "XXXX000101HDFNNN00";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaClienteUseCase consultaClienteUseCase;

    @MockitoBean
    private RegistrarClienteUseCasePort registrarClienteUseCasePort;

    @MockitoBean
    private ActualizarClienteUseCasePort actualizarClienteUseCasePort;

    private Cliente crearClienteMuestra() {
        return new Cliente(
                CURP_VALIDA,
                "Emmett",
                "Brown",
                "Lathrop",
                "Av. Universidad 123",
                LocalDate.of(1955, 1, 1));
    }

    @Test
    void debeResponder201AlRegistrarClienteValido() throws Exception {
        when(registrarClienteUseCasePort.registrarCliente(any(Cliente.class)))
                .thenReturn(crearClienteMuestra());

        String cuerpo = """
                {
                  "curp": "BROE550101HDFABC00",
                  "nombres": "Emmett",
                  "primerApellido": "Brown",
                  "segundoApellido": "Lathrop",
                  "direccion": "Av. Universidad 123",
                  "fechaNacimiento": "1955-01-01"
                }
                """;

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.curp").value(CURP_VALIDA))
                .andExpect(jsonPath("$.nombres").value("Emmett"))
                .andExpect(jsonPath("$.primerApellido").value("Brown"));
    }

    @Test
    void debeResponder200EnConsultasYActualizacionDeCliente() throws Exception {
        Cliente muestra = crearClienteMuestra();
        when(consultaClienteUseCase.consultarPorCurp(CURP_VALIDA))
                .thenReturn(muestra);
        when(consultaClienteUseCase.consultar(any(FiltroCliente.class), eq(0), eq(20)))
                .thenReturn(Pagina.de(List.of(muestra), 0, 20));
        when(actualizarClienteUseCasePort.actualizarCliente(any(Cliente.class)))
                .thenReturn(muestra);

        mockMvc.perform(get("/clientes/{curp}", CURP_VALIDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curp").value(CURP_VALIDA));

        mockMvc.perform(get("/clientes")
                .param("nombres", "Emmett")
                .param("primerApellido", "Brown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.totalElementos").value(1));

        String cuerpoActualizacion = """
                {
                  "curp": "BROE550101HDFABC00",
                  "nombres": "Emmett",
                  "primerApellido": "Brown",
                  "segundoApellido": "Lathrop",
                  "direccion": "Calle Pino 456",
                  "fechaNacimiento": "1955-01-01"
                }
                """;

        mockMvc.perform(put("/clientes/{curp}", CURP_VALIDA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoActualizacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curp").value(CURP_VALIDA));
    }

    @Test
    void debeResponder404CuandoElClienteNoExisteEnGetOPut() throws Exception {
        when(consultaClienteUseCase.consultarPorCurp(CURP_INEXISTENTE))
                .thenThrow(new ClienteNoEncontradoException(CURP_INEXISTENTE));
        when(actualizarClienteUseCasePort.actualizarCliente(any(Cliente.class)))
                .thenThrow(new ClienteNoEncontradoException(CURP_INEXISTENTE));

        mockMvc.perform(get("/clientes/{curp}", CURP_INEXISTENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        String cuerpo = """
                {
                  "curp": "XXXX000101HDFNNN00",
                  "nombres": "Emmett",
                  "primerApellido": "Brown",
                  "direccion": "Av. Universidad 123",
                  "fechaNacimiento": "1955-01-01"
                }
                """;

        mockMvc.perform(put("/clientes/{curp}", CURP_INEXISTENTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void debeResponder409CuandoElClienteYaEstaRegistrado() throws Exception {
        when(registrarClienteUseCasePort.registrarCliente(any(Cliente.class)))
                .thenThrow(new ConflictoDatosException(
                        "El cliente ya se encuentra registrado."));

        String cuerpo = """
                {
                  "curp": "BROE550101HDFABC00",
                  "nombres": "Emmett",
                  "primerApellido": "Brown",
                  "direccion": "Av. Universidad 123",
                  "fechaNacimiento": "1955-01-01"
                }
                """;

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void debeResponder422CuandoCurpODatosDelClienteSonInvalidos() throws Exception {
        String curpCorta = """
                {
                  "curp": "CORTA123",
                  "nombres": "Emmett",
                  "primerApellido": "Brown",
                  "direccion": "Av. Universidad 123",
                  "fechaNacimiento": "1955-01-01"
                }
                """;

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(curpCorta))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        when(registrarClienteUseCasePort.registrarCliente(any(Cliente.class)))
                .thenThrow(new ReglaNegocioException("CURP en minusculas no permitida"));

        String cuerpo = """
                {
                  "curp": "broe550101hdfabc00",
                  "nombres": "Emmett",
                  "primerApellido": "Brown",
                  "direccion": "Av. Universidad 123",
                  "fechaNacimiento": "1955-01-01"
                }
                """;

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }
}
