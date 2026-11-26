package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ConsultaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.FiltroPoliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas WebMvc para {@link PolizaViewController}: verifica el listado
 * paginado con los 6 modos de filtro, la vista de detalle con beneficiarios
 * actuales y las alertas amigables ante 404, 422, 502 y 503.
 */
@WebMvcTest(PolizaViewController.class)
class PolizaViewControllerTest {

    private static final UUID CLAVE_POLIZA
            = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private static final String CURP_MINUSCULA = "broe550101hdfabc00";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaPolizaUseCase consultaPolizaUseCase;

    @MockitoBean
    private ConsultaBeneficiarioUseCase consultaBeneficiarioUseCase;

    private Poliza crearPoliza() {
        return new Poliza(
                CLAVE_POLIZA,
                (byte) 1,
                25000.0f,
                "Seguro de vida familiar",
                CURP_MINUSCULA);
    }

    private Beneficiario crearBeneficiario(String nombres, int porcentaje) {
        return new Beneficiario(
                nombres,
                "Brown",
                "Lathrop",
                LocalDate.of(1990, 1, 1),
                CLAVE_POLIZA,
                porcentaje);
    }

    @Test
    void debeMostrarListadoPaginadoSinFiltro() throws Exception {
        Poliza poliza = crearPoliza();
        when(consultaPolizaUseCase.consultar(any(FiltroPoliza.Todas.class), eq(0), eq(10)))
                .thenReturn(Pagina.de(List.of(poliza), 0, 10));

        mockMvc.perform(get("/web/polizas"))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/lista"))
                .andExpect(model().attributeExists("paginaPolizas"))
                .andExpect(content().string(containsString(CLAVE_POLIZA.toString())))
                .andExpect(content().string(containsString(CURP_MINUSCULA)))
                .andExpect(content().string(containsString("Vida")));
    }

    @Test
    void debeFiltrarPorCurpRespetandoMinusculas() throws Exception {
        Poliza poliza = crearPoliza();
        when(consultaPolizaUseCase.consultar(
                eq(new FiltroPoliza.PorCurp(CURP_MINUSCULA)), eq(0), eq(10)))
                .thenReturn(Pagina.de(List.of(poliza), 0, 10));

        mockMvc.perform(get("/web/polizas")
                .param("tipoFiltro", "curp")
                .param("curp", CURP_MINUSCULA))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/lista"))
                .andExpect(content().string(containsString(CLAVE_POLIZA.toString())));

        verify(consultaPolizaUseCase).consultar(
                new FiltroPoliza.PorCurp(CURP_MINUSCULA), 0, 10);
    }

    @Test
    void debeFiltrarPorTipoFechaClienteYBeneficiario() throws Exception {
        Poliza poliza = crearPoliza();
        when(consultaPolizaUseCase.consultar(any(FiltroPoliza.class), eq(0), eq(10)))
                .thenReturn(Pagina.de(List.of(poliza), 0, 10));

        mockMvc.perform(get("/web/polizas")
                .param("tipoFiltro", "tipo")
                .param("tipo", "1"))
                .andExpect(status().isOk());
        verify(consultaPolizaUseCase).consultar(new FiltroPoliza.PorTipo(1), 0, 10);

        mockMvc.perform(get("/web/polizas")
                .param("tipoFiltro", "fechaBeneficiario")
                .param("fechaBeneficiario", "1990-01-01"))
                .andExpect(status().isOk());
        verify(consultaPolizaUseCase).consultar(
                new FiltroPoliza.PorFechaNacimientoBeneficiario(LocalDate.of(1990, 1, 1)), 0, 10);

        mockMvc.perform(get("/web/polizas")
                .param("tipoFiltro", "cliente")
                .param("nombres", "Emmett")
                .param("primerApellido", "Brown"))
                .andExpect(status().isOk());
        verify(consultaPolizaUseCase).consultar(
                new FiltroPoliza.PorCliente("Emmett", "Brown", null), 0, 10);

        mockMvc.perform(get("/web/polizas")
                .param("tipoFiltro", "beneficiario")
                .param("nombres", "Clara")
                .param("primerApellido", "Clayton")
                .param("segundoApellido", ""))
                .andExpect(status().isOk());
        verify(consultaPolizaUseCase).consultar(
                new FiltroPoliza.PorBeneficiario("Clara", "Clayton", null), 0, 10);
    }

    @Test
    void debeMostrarDetalleConBeneficiariosActuales() throws Exception {
        Poliza poliza = crearPoliza();
        List<Beneficiario> beneficiarios = List.of(
                crearBeneficiario("Clara", 60),
                crearBeneficiario("Jules", 40));
        when(consultaPolizaUseCase.consultarPorClave(CLAVE_POLIZA)).thenReturn(poliza);
        when(consultaBeneficiarioUseCase.listarPorPoliza(CLAVE_POLIZA))
                .thenReturn(beneficiarios);

        mockMvc.perform(get("/web/polizas/{clave}", CLAVE_POLIZA))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/detalle"))
                .andExpect(model().attribute("poliza", poliza))
                .andExpect(model().attribute("sumaPorcentaje", 100))
                .andExpect(model().attribute("porcentajeDisponible", 0))
                .andExpect(content().string(containsString("Seguro de vida familiar")))
                .andExpect(content().string(containsString("Clara")))
                .andExpect(content().string(containsString("60%")))
                .andExpect(content().string(containsString("Jules")));

        mockMvc.perform(get("/web/polizas/consultar")
                .param("clave", CLAVE_POLIZA.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/detalle"))
                .andExpect(content().string(containsString("Clara")));
    }

    @Test
    void debeMostrarAlerta404CuandoPolizaNoExiste() throws Exception {
        when(consultaPolizaUseCase.consultarPorClave(CLAVE_POLIZA))
                .thenThrow(new PolizaNoEncontradaException(CLAVE_POLIZA));

        mockMvc.perform(get("/web/polizas/{clave}", CLAVE_POLIZA))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/detalle"))
                .andExpect(content().string(containsString("HTTP 404")));
    }

    @Test
    void debeMostrarAlerta422AnteParametrosInvalidos() throws Exception {
        mockMvc.perform(get("/web/polizas/consultar").param("clave", "no-es-uuid"))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/detalle"))
                .andExpect(content().string(containsString("HTTP 422")));

        mockMvc.perform(get("/web/polizas")
                .param("tipoFiltro", "tipo")
                .param("tipo", "9"))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/lista"))
                .andExpect(content().string(containsString("HTTP 422")));

        mockMvc.perform(get("/web/polizas")
                .param("tipoFiltro", "fechaBeneficiario")
                .param("fechaBeneficiario", LocalDate.now().plusYears(10).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/lista"))
                .andExpect(content().string(containsString("HTTP 422")));
    }

    @Test
    void debeMostrarAlertas502Y503SinFiltrarDetallesInternos() throws Exception {
        when(consultaPolizaUseCase.consultar(any(FiltroPoliza.class), eq(0), eq(10)))
                .thenThrow(new SistemaDuenoRechazoException(
                        "HTTP 500 en http://nachintoch.mx:8080/polizas"));

        mockMvc.perform(get("/web/polizas"))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/lista"))
                .andExpect(content().string(containsString("HTTP 502")))
                .andExpect(content().string(not(containsString("nachintoch.mx"))));

        when(consultaPolizaUseCase.consultar(any(FiltroPoliza.class), eq(0), eq(10)))
                .thenThrow(new SistemaDuenoNoDisponibleException(
                        "Timeout con http://nachintoch.mx:8080"));

        mockMvc.perform(get("/web/polizas"))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/lista"))
                .andExpect(content().string(containsString("HTTP 503")))
                .andExpect(content().string(not(containsString("nachintoch.mx"))));
    }
}
