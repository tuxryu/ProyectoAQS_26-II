package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CancelarPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultarCatalogoClientesUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CrearPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ModificacionPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
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
 * Pruebas WebMvc para {@link PolizaEscrituraViewController}: verifica alta con
 * selector de CURP, modificacion, baja y alertas amigables ante 404, 409, 422,
 * 502 y 503.
 */
@WebMvcTest(PolizaEscrituraViewController.class)
class PolizaEscrituraViewControllerTest {

    private static final UUID CLAVE_POLIZA
            = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private static final String CURP_VALIDA = "BROE550101HDFABC00";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrearPolizaUseCasePort crearPolizaUseCase;

    @MockitoBean
    private ModificacionPolizaUseCasePort modificacionPolizaUseCase;

    @MockitoBean
    private CancelarPolizaUseCasePort cancelarPolizaUseCase;

    @MockitoBean
    private ConsultaPolizaUseCase consultaPolizaUseCase;

    @MockitoBean
    private ConsultarCatalogoClientesUseCase consultarCatalogoClientesUseCase;

    private Cliente crearCliente() {
        return new Cliente(
                CURP_VALIDA,
                "Emmett",
                "Brown",
                "Lathrop",
                "Av. Universidad 123",
                LocalDate.of(1955, 1, 1));
    }

    private Poliza crearPoliza() {
        return new Poliza(
                CLAVE_POLIZA,
                (byte) 0,
                15000.0f,
                "Seguro vehicular",
                CURP_VALIDA);
    }

    @Test
    void debeMostrarFormularioDeAltaConSelectorDeCurp() throws Exception {
        doReturn(List.of(crearCliente()))
                .when(consultarCatalogoClientesUseCase).obtenerCatalogo();

        mockMvc.perform(get("/web/polizas/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/formulario"))
                .andExpect(model().attribute("modoEdicion", false))
                .andExpect(content().string(containsString(CURP_VALIDA)))
                .andExpect(content().string(containsString("Emmett")));
    }

    @Test
    void debeRegistrarPolizaYRedirigirMostrandoClaveGenerada() throws Exception {
        when(crearPolizaUseCase.crearPoliza(any(Poliza.class)))
                .thenReturn(crearPoliza());

        mockMvc.perform(post("/web/polizas/nuevo")
                .param("tipo", "0")
                .param("monto", "15000.00")
                .param("curpCliente", CURP_VALIDA)
                .param("descripcion", "Seguro vehicular"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/polizas/consultar?clave=" + CLAVE_POLIZA))
                .andExpect(flash().attributeExists("alerta"));
    }

    @Test
    void debeCargarYActualizarPolizaExistente() throws Exception {
        Poliza existente = crearPoliza();
        doReturn(List.of(crearCliente()))
                .when(consultarCatalogoClientesUseCase).obtenerCatalogo();
        when(consultaPolizaUseCase.consultarPorClave(CLAVE_POLIZA))
                .thenReturn(existente);
        when(modificacionPolizaUseCase.modificarPoliza(eq(CLAVE_POLIZA), any(Poliza.class)))
                .thenReturn(existente);

        mockMvc.perform(get("/web/polizas/{clave}/editar", CLAVE_POLIZA))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/formulario"))
                .andExpect(model().attribute("modoEdicion", true))
                .andExpect(content().string(containsString("Seguro vehicular")));

        mockMvc.perform(post("/web/polizas/{clave}/editar", CLAVE_POLIZA)
                .param("tipo", "1")
                .param("monto", "20000.00")
                .param("curpCliente", CURP_VALIDA)
                .param("descripcion", "Cobertura ampliada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/polizas/consultar?clave=" + CLAVE_POLIZA))
                .andExpect(flash().attributeExists("alerta"));
    }

    @Test
    void debeCancelarPolizaYRedirigirConAlertaDeExito() throws Exception {
        mockMvc.perform(post("/web/polizas/{clave}/cancelar", CLAVE_POLIZA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/polizas/consultar"))
                .andExpect(flash().attributeExists("alerta"));

        verify(cancelarPolizaUseCase).cancelarPoliza(CLAVE_POLIZA);
    }

    @Test
    void debeMostrarAlertas404Y409Y422() throws Exception {
        doReturn(List.of()).when(consultarCatalogoClientesUseCase).obtenerCatalogo();

        when(consultaPolizaUseCase.consultarPorClave(CLAVE_POLIZA))
                .thenThrow(new PolizaNoEncontradaException(CLAVE_POLIZA));
        mockMvc.perform(get("/web/polizas/{clave}/editar", CLAVE_POLIZA))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/buscar-editar"))
                .andExpect(content().string(containsString("HTTP 404")));

        when(crearPolizaUseCase.crearPoliza(any(Poliza.class)))
                .thenThrow(new ConflictoDatosException("Ya existe una poliza con esa clave."));
        mockMvc.perform(post("/web/polizas/nuevo")
                .param("tipo", "0")
                .param("monto", "15000.00")
                .param("curpCliente", CURP_VALIDA)
                .param("descripcion", "Seguro vehicular"))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/formulario"))
                .andExpect(content().string(containsString("HTTP 409")));

        mockMvc.perform(post("/web/polizas/nuevo")
                .param("tipo", "9")
                .param("monto", "-50")
                .param("curpCliente", CURP_VALIDA))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/formulario"))
                .andExpect(content().string(containsString("HTTP 422")));
    }

    @Test
    void debeMostrarAlertas502Y503SinFiltrarDetallesInternos() throws Exception {
        doReturn(List.of()).when(consultarCatalogoClientesUseCase).obtenerCatalogo();

        when(crearPolizaUseCase.crearPoliza(any(Poliza.class)))
                .thenThrow(new SistemaDuenoRechazoException(
                        "HTTP 500 en http://nachintoch.mx:8080/poliza"));
        mockMvc.perform(post("/web/polizas/nuevo")
                .param("tipo", "0")
                .param("monto", "15000.00")
                .param("curpCliente", CURP_VALIDA)
                .param("descripcion", "Seguro vehicular"))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/formulario"))
                .andExpect(content().string(containsString("HTTP 502")))
                .andExpect(content().string(not(containsString("nachintoch.mx"))));

        when(crearPolizaUseCase.crearPoliza(any(Poliza.class)))
                .thenThrow(new SistemaDuenoNoDisponibleException(
                        "Timeout con http://nachintoch.mx:8080"));
        mockMvc.perform(post("/web/polizas/nuevo")
                .param("tipo", "0")
                .param("monto", "15000.00")
                .param("curpCliente", CURP_VALIDA)
                .param("descripcion", "Seguro vehicular"))
                .andExpect(status().isOk())
                .andExpect(view().name("polizas/formulario"))
                .andExpect(content().string(containsString("HTTP 503")))
                .andExpect(content().string(not(containsString("nachintoch.mx"))));
    }
}
