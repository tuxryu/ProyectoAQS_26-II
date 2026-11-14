package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.in.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ConsultarBeneficiarioUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.RegistrarBeneficiarioUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion WebMvc (@WebMvcTest) para {@link BeneficiarioController}:
 * verifica los codigos HTTP 201, 200 (listado y consulta individual), 404, 409
 * y 422 junto con GlobalExceptionHandler.
 */
@WebMvcTest(BeneficiarioController.class)
class BeneficiarioControllerTest {

    private static final UUID CLAVE_EXISTENTE
            = UUID.fromString("b97fd779-2e26-452e-b947-24505ceb82ec");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarBeneficiarioUseCasePort registrarBeneficiarioUseCasePort;

    @MockitoBean
    private ConsultarBeneficiarioUseCasePort consultarBeneficiarioUseCasePort;

    private Beneficiario crearBeneficiarioMuestra() {
        return new Beneficiario(
                "Mario",
                "Prueba",
                "Lopez",
                LocalDate.of(2000, 2, 2),
                CLAVE_EXISTENTE,
                60);
    }

    @Test
    void debeResponder201AlRegistrarBeneficiarioValido() throws Exception {
        when(registrarBeneficiarioUseCasePort.registrarBeneficiario(
                any(Beneficiario.class)))
                .thenReturn(crearBeneficiarioMuestra());

        String cuerpo = """
                {
                  "nombres": "Mario",
                  "primerApellido": "Prueba",
                  "segundoApellido": "Lopez",
                  "fechaNacimiento": "2000-02-02",
                  "porcentaje": 60
                }
                """;

        mockMvc.perform(post("/polizas/{clave}/beneficiarios", CLAVE_EXISTENTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombres").value("Mario"))
                .andExpect(jsonPath("$.porcentaje").value(60));
    }

    @Test
    void debeResponder200ConListadoYConsultaIndividualDeBeneficiarios() throws Exception {
        when(consultarBeneficiarioUseCasePort.consultarPorPoliza(
                CLAVE_EXISTENTE.toString()))
                .thenReturn(List.of(crearBeneficiarioMuestra()));

        mockMvc.perform(get("/polizas/{clave}/beneficiarios", CLAVE_EXISTENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombres").value("Mario"))
                .andExpect(jsonPath("$[0].porcentaje").value(60));

        mockMvc.perform(get("/polizas/{clave}/beneficiarios", CLAVE_EXISTENTE)
                .param("nombres", "Mario")
                .param("primerApellido", "Prueba")
                .param("segundoApellido", "Lopez")
                .param("fechaNacimiento", "2000-02-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Mario"))
                .andExpect(jsonPath("$.porcentaje").value(60));
    }

    @Test
    void debeResponder404CuandoPolizaOBeneficiarioNoExiste() throws Exception {
        when(consultarBeneficiarioUseCasePort.consultarPorPoliza(
                CLAVE_EXISTENTE.toString()))
                .thenThrow(new RecursoNoEncontradoException(
                        "No existe la poliza con clave " + CLAVE_EXISTENTE));

        mockMvc.perform(get("/polizas/{clave}/beneficiarios", CLAVE_EXISTENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void debeResponder409CuandoElBeneficiarioEstaDuplicado() throws Exception {
        when(registrarBeneficiarioUseCasePort.registrarBeneficiario(
                any(Beneficiario.class)))
                .thenThrow(new ConflictoDatosException(
                        "El beneficiario ya se encuentra registrado."));

        String cuerpo = """
                {
                  "nombres": "Mario",
                  "primerApellido": "Prueba",
                  "fechaNacimiento": "2000-02-02",
                  "porcentaje": 60
                }
                """;

        mockMvc.perform(post("/polizas/{clave}/beneficiarios", CLAVE_EXISTENTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void debeResponder422CuandoSumaDePorcentajesExcede100OPorcentajeEsInvalido()
            throws Exception {
        String porcentajeFueraDeRango = """
                {
                  "nombres": "Ana",
                  "primerApellido": "Gomez",
                  "fechaNacimiento": "1998-04-15",
                  "porcentaje": 150
                }
                """;

        mockMvc.perform(post("/polizas/{clave}/beneficiarios", CLAVE_EXISTENTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(porcentajeFueraDeRango))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        when(registrarBeneficiarioUseCasePort.registrarBeneficiario(
                any(Beneficiario.class)))
                .thenThrow(new ReglaNegocioException("La suma excede el 100%."));

        String cuerpo = """
                {
                  "nombres": "Ana",
                  "primerApellido": "Gomez",
                  "fechaNacimiento": "1998-04-15",
                  "porcentaje": 50
                }
                """;

        mockMvc.perform(post("/polizas/{clave}/beneficiarios", CLAVE_EXISTENTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }
}
