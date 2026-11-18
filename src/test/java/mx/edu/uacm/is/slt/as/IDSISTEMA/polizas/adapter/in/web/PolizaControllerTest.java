package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.in.web;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CancelarPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CrearPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.FiltroPoliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ModificacionPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.FuncionalidadNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion WebMvc (@WebMvcTest) para {@link PolizaController} y
 * {@link PolizaEscrituraController}: verifica lectura, filtros GET y escritura
 * (POST, PUT, DELETE) con los codigos HTTP 200, 201, 204, 400, 404, 409, 422,
 * 501, 502 y 503 junto con GlobalExceptionHandler.
 */
@WebMvcTest({PolizaController.class, PolizaEscrituraController.class})
class PolizaControllerTest {

    private static final UUID CLAVE_EXISTENTE
            = UUID.fromString("b97fd779-2e26-452e-b947-24505ceb82ec");

    private static final UUID CLAVE_INEXISTENTE
            = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final String CURP_MUESTRA = "broe550101hdfabc00";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaPolizaUseCase consultaPolizaUseCase;

    @MockitoBean
    private CrearPolizaUseCasePort crearPolizaUseCasePort;

    @MockitoBean
    private ModificacionPolizaUseCasePort modificacionPolizaUseCasePort;

    @MockitoBean
    private CancelarPolizaUseCasePort cancelarPolizaUseCasePort;

    private Poliza crearPolizaMuestra() {
        return new Poliza(
                CLAVE_EXISTENTE,
                (byte) 2,
                1500000.0f,
                "Cobertura por accidentes industriales",
                CURP_MUESTRA);
    }

    @Test
    void debeResponder200AlConsultarPolizaExistentePorClave() throws Exception {
        when(consultaPolizaUseCase.consultarPorClave(CLAVE_EXISTENTE))
                .thenReturn(crearPolizaMuestra());

        mockMvc.perform(get("/polizas/{clave}", CLAVE_EXISTENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clave").value(CLAVE_EXISTENTE.toString()))
                .andExpect(jsonPath("$.tipo").value(2))
                .andExpect(jsonPath("$.curpCliente").value(CURP_MUESTRA));
    }

    @Test
    void debeResponder404CuandoLaPolizaPorClaveNoExiste() throws Exception {
        when(consultaPolizaUseCase.consultarPorClave(CLAVE_INEXISTENTE))
                .thenThrow(new PolizaNoEncontradaException(CLAVE_INEXISTENTE));

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
        when(consultaPolizaUseCase.consultar(any(FiltroPoliza.class), eq(0), eq(10)))
                .thenReturn(Pagina.de(List.of(crearPolizaMuestra()), 0, 10));

        mockMvc.perform(get("/polizas")
                .param("curp", CURP_MUESTRA)
                .param("pagina", "0")
                .param("tamano", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void debeResponder200EnRutasDeFiltrosPorCurpTipoClienteYBeneficiario() throws Exception {
        when(consultaPolizaUseCase.consultar(any(FiltroPoliza.class), anyInt(), anyInt()))
                .thenReturn(Pagina.de(List.of(crearPolizaMuestra()), 0, 20));

        mockMvc.perform(get("/polizas/curp/{curp}", CURP_MUESTRA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1));

        mockMvc.perform(get("/polizas/tipo/{tipo}", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1));

        mockMvc.perform(get("/polizas/c/{nombres}/{primerApellido}", "Emmett", "Brown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1));

        mockMvc.perform(get(
                "/polizas/c/{nombres}/{primerApellido}/{segundoApellido}",
                "Emmett",
                "Brown",
                "Lathrop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1));

        mockMvc.perform(get("/polizas/b/{nombres}/{primerApellido}", "Clara", "Clayton"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1));

        mockMvc.perform(get(
                "/polizas/b/{nombres}/{primerApellido}/{segundoApellido}",
                "Clara",
                "Clayton",
                "Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1));
    }

    @Test
    void debeResponder501AlBuscarPorFechaDeNacimientoDeBeneficiario() throws Exception {
        when(consultaPolizaUseCase.consultar(
                any(FiltroPoliza.PorFechaNacimientoBeneficiario.class), anyInt(), anyInt()))
                .thenThrow(new FuncionalidadNoDisponibleException("No disponible"));

        mockMvc.perform(get("/polizas/b/fecha/{fecha}", "2000-02-02"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.status").value(501));
    }

    @Test
    void debeResponder400CuandoSeEnviaMasDeUnFiltro() throws Exception {
        mockMvc.perform(get("/polizas")
                .param("curp", CURP_MUESTRA)
                .param("tipo", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void debeResponder422CuandoElTipoEstaFueraDeRangoOTieneDiagonal() throws Exception {
        mockMvc.perform(get("/polizas").param("tipo", "9"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        mockMvc.perform(get("/polizas/tipo/{tipo}", 9))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        mockMvc.perform(get("/polizas").param("curp", "BROE/550101"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void debeResponder200EnBusquedaPorClienteYBeneficiario() throws Exception {
        when(consultaPolizaUseCase.consultar(any(FiltroPoliza.class), anyInt(), anyInt()))
                .thenReturn(Pagina.de(List.of(crearPolizaMuestra()), 0, 20));

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
        when(consultaPolizaUseCase.consultar(any(FiltroPoliza.class), anyInt(), anyInt()))
                .thenThrow(new SistemaDuenoRechazoException("Rechazo 500"))
                .thenThrow(new SistemaDuenoNoDisponibleException("Timeout"));

        mockMvc.perform(get("/polizas"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));

        mockMvc.perform(get("/polizas"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void debeResponder201AlRegistrarPolizaValida() throws Exception {
        when(crearPolizaUseCasePort.crearPoliza(any(Poliza.class)))
                .thenReturn(crearPolizaMuestra());

        mockMvc.perform(post(
                "/poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}",
                CLAVE_EXISTENTE,
                2,
                1500000.0f,
                "Cobertura por accidentes industriales",
                CURP_MUESTRA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clave").value(CLAVE_EXISTENTE.toString()))
                .andExpect(jsonPath("$.tipo").value(2))
                .andExpect(jsonPath("$.curpCliente").value(CURP_MUESTRA));
    }

    @Test
    void debeResponder404Y409Y422EnRegistroDePoliza() throws Exception {
        when(crearPolizaUseCasePort.crearPoliza(any(Poliza.class)))
                .thenThrow(new RecursoNoEncontradoException("Cliente no encontrado"))
                .thenThrow(new ConflictoDatosException("Poliza duplicada"))
                .thenThrow(new ReglaNegocioException("Datos de poliza invalidos"));

        mockMvc.perform(post(
                "/poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}",
                CLAVE_EXISTENTE,
                2,
                1500000.0f,
                "Cobertura",
                CURP_MUESTRA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(post(
                "/poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}",
                CLAVE_EXISTENTE,
                2,
                1500000.0f,
                "Cobertura",
                CURP_MUESTRA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mockMvc.perform(post(
                "/poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}",
                CLAVE_EXISTENTE,
                2,
                1500000.0f,
                "Cobertura",
                CURP_MUESTRA))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void debeResponder200Y404AlActualizarPoliza() throws Exception {
        when(modificacionPolizaUseCasePort.modificarPoliza(
                eq(CLAVE_EXISTENTE), any(Poliza.class)))
                .thenReturn(crearPolizaMuestra());
        when(modificacionPolizaUseCasePort.modificarPoliza(
                eq(CLAVE_INEXISTENTE), any(Poliza.class)))
                .thenThrow(new PolizaNoEncontradaException(CLAVE_INEXISTENTE));

        mockMvc.perform(put(
                "/poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}",
                CLAVE_EXISTENTE,
                2,
                1500000.0f,
                "Cobertura actualizada",
                CURP_MUESTRA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clave").value(CLAVE_EXISTENTE.toString()));

        mockMvc.perform(put(
                "/poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}",
                CLAVE_INEXISTENTE,
                2,
                1500000.0f,
                "Cobertura actualizada",
                CURP_MUESTRA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void debeResponder204Y404AlEliminarPoliza() throws Exception {
        doNothing().when(cancelarPolizaUseCasePort).cancelarPoliza(CLAVE_EXISTENTE);
        doThrow(new PolizaNoEncontradaException(CLAVE_INEXISTENTE))
                .when(cancelarPolizaUseCasePort).cancelarPoliza(CLAVE_INEXISTENTE);

        mockMvc.perform(delete("/poliza/{clave}", CLAVE_EXISTENTE))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/poliza/{clave}", CLAVE_INEXISTENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
