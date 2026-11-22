package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.AltaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ConsultaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
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
 * Pruebas WebMvc para {@link BeneficiarioViewController}: verifica listado por
 * poliza con suma actual y porcentaje disponible hasta 100%, busqueda
 * individual, alta vinculada y alertas amigables ante 404, 409, 422, 502 y 503.
 */
@WebMvcTest(BeneficiarioViewController.class)
class BeneficiarioViewControllerTest {

    private static final UUID CLAVE_POLIZA
            = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AltaBeneficiarioUseCase altaBeneficiarioUseCase;

    @MockitoBean
    private ConsultaBeneficiarioUseCase consultaBeneficiarioUseCase;

    private Beneficiario crearBeneficiario(String nombres, int porcentaje) {
        return new Beneficiario(
                nombres,
                "Lopez",
                "Perez",
                LocalDate.of(1992, 5, 10),
                CLAVE_POLIZA,
                porcentaje);
    }

    @Test
    void debeMostrarTablaConSumaActualYPorcentajeDisponible() throws Exception {
        List<Beneficiario> lista = List.of(
                crearBeneficiario("Ana", 40),
                crearBeneficiario("Carlos", 25));
        when(consultaBeneficiarioUseCase.listarPorPoliza(CLAVE_POLIZA))
                .thenReturn(lista);

        mockMvc.perform(get("/web/beneficiarios")
                .param("clavePoliza", CLAVE_POLIZA.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficiarios/lista"))
                .andExpect(model().attribute("polizaConsultada", true))
                .andExpect(model().attribute("sumaPorcentaje", 65))
                .andExpect(model().attribute("porcentajeDisponible", 35))
                .andExpect(model().attribute("cupoCompleto", false))
                .andExpect(content().string(containsString("65%")))
                .andExpect(content().string(containsString("35%")))
                .andExpect(content().string(containsString("Ana")))
                .andExpect(content().string(containsString("Carlos")));
    }

    @Test
    void debeMarcarCupoCompletoCuandoLaSumaEsCienPorCiento() throws Exception {
        when(consultaBeneficiarioUseCase.listarPorPoliza(CLAVE_POLIZA))
                .thenReturn(List.of(crearBeneficiario("Ana", 100)));

        mockMvc.perform(get("/web/beneficiarios")
                .param("clavePoliza", CLAVE_POLIZA.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sumaPorcentaje", 100))
                .andExpect(model().attribute("porcentajeDisponible", 0))
                .andExpect(model().attribute("cupoCompleto", true))
                .andExpect(content().string(containsString("Porcentaje completo (100%)")));
    }

    @Test
    void debeConsultarBeneficiarioIndividualPorLlaveCompuesta() throws Exception {
        Beneficiario ben = crearBeneficiario("Ana", 50);
        when(consultaBeneficiarioUseCase.consultarPorDatos(
                CLAVE_POLIZA, "Ana", "Lopez", "Perez", LocalDate.of(1992, 5, 10)))
                .thenReturn(ben);

        mockMvc.perform(get("/web/beneficiarios/consultar")
                .param("clavePoliza", CLAVE_POLIZA.toString())
                .param("nombres", "Ana")
                .param("primerApellido", "Lopez")
                .param("segundoApellido", "Perez")
                .param("fechaNacimiento", "1992-05-10"))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficiarios/detalle"))
                .andExpect(model().attribute("beneficiario", ben))
                .andExpect(content().string(containsString("50%")));
    }

    @Test
    void debeMostrarFormularioDeAltaVinculadaConPorcentajeDisponible() throws Exception {
        when(consultaBeneficiarioUseCase.listarPorPoliza(CLAVE_POLIZA))
                .thenReturn(List.of(crearBeneficiario("Ana", 70)));

        mockMvc.perform(get("/web/beneficiarios/nuevo")
                .param("clavePoliza", CLAVE_POLIZA.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficiarios/formulario"))
                .andExpect(model().attribute("sumaPorcentaje", 70))
                .andExpect(model().attribute("porcentajeDisponible", 30))
                .andExpect(content().string(containsString(CLAVE_POLIZA.toString())))
                .andExpect(content().string(containsString("30%")));
    }

    @Test
    void debeRegistrarBeneficiarioYRedirigirAlListadoDeLaPoliza() throws Exception {
        Beneficiario creado = crearBeneficiario("Ana", 30);
        when(altaBeneficiarioUseCase.registrar(any(Beneficiario.class)))
                .thenReturn(creado);

        mockMvc.perform(post("/web/beneficiarios/nuevo")
                .param("clavePoliza", CLAVE_POLIZA.toString())
                .param("nombres", "Ana")
                .param("primerApellido", "Lopez")
                .param("segundoApellido", "Perez")
                .param("fechaNacimiento", "1992-05-10")
                .param("porcentaje", "30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/beneficiarios?clavePoliza=" + CLAVE_POLIZA))
                .andExpect(flash().attributeExists("alerta"));
    }

    @Test
    void debeMostrarAlerta404CuandoPolizaOBeneficiarioNoExiste() throws Exception {
        when(consultaBeneficiarioUseCase.listarPorPoliza(CLAVE_POLIZA))
                .thenThrow(new RecursoNoEncontradoException(
                        "No existe la poliza con clave " + CLAVE_POLIZA + "."));

        mockMvc.perform(get("/web/beneficiarios")
                .param("clavePoliza", CLAVE_POLIZA.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficiarios/lista"))
                .andExpect(content().string(containsString("HTTP 404")));
    }

    @Test
    void debeMostrarAlerta409CuandoElBeneficiarioYaExiste() throws Exception {
        when(altaBeneficiarioUseCase.registrar(any(Beneficiario.class)))
                .thenThrow(new ConflictoDatosException(
                        "El beneficiario ya se encuentra registrado en la poliza."));

        mockMvc.perform(post("/web/beneficiarios/nuevo")
                .param("clavePoliza", CLAVE_POLIZA.toString())
                .param("nombres", "Ana")
                .param("primerApellido", "Lopez")
                .param("fechaNacimiento", "1992-05-10")
                .param("porcentaje", "30"))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficiarios/formulario"))
                .andExpect(content().string(containsString("HTTP 409")))
                .andExpect(content().string(containsString("Ana")));
    }

    @Test
    void debeMostrarAlerta422AnteDatosInvalidosOExcesoDePorcentaje() throws Exception {
        mockMvc.perform(post("/web/beneficiarios/nuevo")
                .param("clavePoliza", CLAVE_POLIZA.toString())
                .param("nombres", "Ana")
                .param("primerApellido", "Lopez")
                .param("fechaNacimiento", "1992-05-10")
                .param("porcentaje", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficiarios/formulario"))
                .andExpect(content().string(containsString("HTTP 422")));

        when(altaBeneficiarioUseCase.registrar(any(Beneficiario.class)))
                .thenThrow(new ReglaNegocioException(
                        "La suma de porcentajes excede el 100%."));

        mockMvc.perform(post("/web/beneficiarios/nuevo")
                .param("clavePoliza", CLAVE_POLIZA.toString())
                .param("nombres", "Ana")
                .param("primerApellido", "Lopez")
                .param("fechaNacimiento", "1992-05-10")
                .param("porcentaje", "80"))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficiarios/formulario"))
                .andExpect(content().string(containsString("HTTP 422")));
    }

    @Test
    void debeMostrarAlertas502Y503SinFiltrarDetallesInternos() throws Exception {
        when(altaBeneficiarioUseCase.registrar(any(Beneficiario.class)))
                .thenThrow(new SistemaDuenoRechazoException(
                        "HTTP 500 en http://nachintoch.mx:8080/beneficiario"));

        mockMvc.perform(post("/web/beneficiarios/nuevo")
                .param("clavePoliza", CLAVE_POLIZA.toString())
                .param("nombres", "Ana")
                .param("primerApellido", "Lopez")
                .param("fechaNacimiento", "1992-05-10")
                .param("porcentaje", "20"))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficiarios/formulario"))
                .andExpect(content().string(containsString("HTTP 502")))
                .andExpect(content().string(not(containsString("nachintoch.mx"))));

        when(consultaBeneficiarioUseCase.listarPorPoliza(CLAVE_POLIZA))
                .thenThrow(new SistemaDuenoNoDisponibleException(
                        "Timeout con http://nachintoch.mx:8080"));

        mockMvc.perform(get("/web/beneficiarios")
                .param("clavePoliza", CLAVE_POLIZA.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficiarios/lista"))
                .andExpect(content().string(containsString("HTTP 503")))
                .andExpect(content().string(not(containsString("nachintoch.mx"))));
    }
}
