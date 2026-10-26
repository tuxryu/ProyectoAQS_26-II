package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.in.web;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.FiltroPoliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.adapter.in.web.GlobalExceptionHandler;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.FuncionalidadNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de PolizaController y GlobalExceptionHandler con MockMvc:
 * verifica codigos HTTP 200, 400, 404, 422, 501, 502 y 503.
 */
class PolizaControllerTest {

    private static final UUID CLAVE_EXISTENTE
            = UUID.fromString("b97fd779-2e26-452e-b947-24505ceb82ec");

    private static final UUID CLAVE_INEXISTENTE
            = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private MockMvc mockMvc;
    private CasoDeUsoFalso casoDeUso;

    @BeforeEach
    void configurar() {
        casoDeUso = new CasoDeUsoFalso();
        PolizaController controller = new PolizaController(casoDeUso);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void debeResponder200AlConsultarPolizaExistentePorClave() throws Exception {
        mockMvc.perform(get("/polizas/{clave}", CLAVE_EXISTENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clave").value(CLAVE_EXISTENTE.toString()))
                .andExpect(jsonPath("$.tipo").value(2))
                .andExpect(jsonPath("$.curpCliente").value("broe550101hdfabc00"));
    }

    @Test
    void debeResponder404CuandoLaPolizaPorClaveNoExiste() throws Exception {
        mockMvc.perform(get("/polizas/{clave}", CLAVE_INEXISTENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void debeResponder400CuandoElUuidEnRutaEsIlegible() throws Exception {
        mockMvc.perform(get("/polizas/no-es-un-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void debeResponder200ConPaginaAlListarPorCurp() throws Exception {
        mockMvc.perform(get("/polizas")
                .param("curp", "broe550101hdfabc00")
                .param("pagina", "0")
                .param("tamano", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void debeResponder400CuandoSeEnviaMasDeUnFiltro() throws Exception {
        mockMvc.perform(get("/polizas")
                .param("curp", "broe550101hdfabc00")
                .param("tipo", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void debeResponder422CuandoElTipoEstaFueraDeRangoOTieneDiagonal() throws Exception {
        mockMvc.perform(get("/polizas").param("tipo", "9"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        mockMvc.perform(get("/polizas").param("curp", "BROE/550101"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void debeResponder200EnBusquedaPorClienteYBeneficiario() throws Exception {
        mockMvc.perform(get("/polizas/busqueda/cliente")
                .param("nombres", "Emmett")
                .param("primerApellido", "Brown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1));

        mockMvc.perform(get("/polizas/busqueda/beneficiario")
                .param("nombres", "Clara")
                .param("primerApellido", "Clayton")
                .param("segundoApellido", "Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1));
    }

    @Test
    void debeResponder400CuandoFaltaParametroObligatorioEnBusquedaPorCliente() throws Exception {
        mockMvc.perform(get("/polizas/busqueda/cliente")
                .param("nombres", "Emmett"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void debeResponder502Y503CuandoFallaElSistemaDueno() throws Exception {
        casoDeUso.errorSimulado = new SistemaDuenoRechazoException("Rechazo 500");
        mockMvc.perform(get("/polizas"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));

        casoDeUso.errorSimulado = new SistemaDuenoNoDisponibleException("Timeout");
        mockMvc.perform(get("/polizas"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    private static final class CasoDeUsoFalso implements ConsultaPolizaUseCase {

        private RuntimeException errorSimulado;

        private final Poliza muestra = new Poliza(
                CLAVE_EXISTENTE,
                (byte) 2,
                1500000.0f,
                "Cobertura por accidentes industriales",
                "broe550101hdfabc00");

        @Override
        public Poliza consultarPorClave(UUID clave) {
            if (errorSimulado != null) {
                throw errorSimulado;
            }
            if (!CLAVE_EXISTENTE.equals(clave)) {
                throw new PolizaNoEncontradaException(clave);
            }
            return muestra;
        }

        @Override
        public Pagina<Poliza> consultar(FiltroPoliza filtro, int pagina, int tamano) {
            if (errorSimulado != null) {
                throw errorSimulado;
            }
            if (filtro instanceof FiltroPoliza.PorFechaNacimientoBeneficiario) {
                throw new FuncionalidadNoDisponibleException("No disponible");
            }
            return Pagina.de(List.of(muestra), pagina, tamano);
        }
    }
}
