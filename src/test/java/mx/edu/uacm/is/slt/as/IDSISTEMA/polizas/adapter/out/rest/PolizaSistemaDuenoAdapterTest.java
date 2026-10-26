package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.out.rest;

import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http.FechasSistemaDueno;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Pruebas unitarias con MockRestServiceServer para las consultas GET de
 * PolizaSistemaDuenoAdapter. Los JSON replican respuestas reales del Sistema Dueno.
 */
class PolizaSistemaDuenoAdapterTest {

    private static final String URL_BASE = "http://nachintoch.mx:8080";

    private static final String CLAVE_MEDICO = "b97fd779-2e26-452e-b947-24505ceb82ec";

    private static final String CLAVE_AUTO = "c6fdd423-176b-4a26-a415-033c738fa896";

    /** Letra e con acento agudo; el archivo debe ser US-ASCII por Checkstyle. */
    private static final char E_ACENTO = (char) 0xE9;

    private static final String POLIZA_MEDICO = """
            {
              "clave": "b97fd779-2e26-452e-b947-24505ceb82ec",
              "tipo": 2,
              "monto": 1500000.0,
              "descripcion": "Cobertura por accidentes industriales",
              "curpCliente": "broe550101hdfabc00"
            }
            """;

    private static final String POLIZA_AUTO = """
            {
              "clave": "c6fdd423-176b-4a26-a415-033c738fa896",
              "tipo": 0,
              "monto": 1000000.0,
              "descripcion": "1983 DMC DeLorean modificado",
              "curpCliente": "broe550101hdfabc00"
            }
            """;

    private MockRestServiceServer mockServer;
    private PolizaSistemaDuenoAdapter adapter;

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(URL_BASE);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        adapter = new PolizaSistemaDuenoAdapter(builder.build());
    }

    @Test
    void debeConsultarPolizaPorClaveYMapearTodosLosCampos() {
        mockServer.expect(requestTo(URL_BASE + "/poliza/" + CLAVE_MEDICO))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(POLIZA_MEDICO, MediaType.APPLICATION_JSON));

        Optional<Poliza> resultado = adapter.buscarPorClave(
                UUID.fromString(CLAVE_MEDICO));

        assertTrue(resultado.isPresent());
        Poliza poliza = resultado.get();
        assertEquals(UUID.fromString(CLAVE_MEDICO), poliza.getClave());
        assertEquals((byte) 2, poliza.getTipo());
        assertEquals(1500000.0f, poliza.getMonto());
        assertEquals("Cobertura por accidentes industriales", poliza.getDescripcion());
        assertEquals("broe550101hdfabc00", poliza.getCurpCliente());
        mockServer.verify();
    }

    @Test
    void debeDevolverOptionalEmptyCuandoLecturaPorClaveResponde200Vacio() {
        UUID clave = UUID.fromString("00000000-0000-0000-0000-000000000000");
        mockServer.expect(requestTo(URL_BASE + "/poliza/" + clave))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertTrue(adapter.buscarPorClave(clave).isEmpty());
        mockServer.verify();
    }

    @Test
    void debeLanzarRechazoCuandoLaPolizaPorClaveTieneDatosInconsistentes() {
        String json = """
                {
                  "clave": "b97fd779-2e26-452e-b947-24505ceb82ec",
                  "tipo": 7,
                  "monto": 100.0,
                  "descripcion": "Basura",
                  "curpCliente": "broe550101hdfabc00"
                }
                """;
        mockServer.expect(requestTo(URL_BASE + "/poliza/" + CLAVE_MEDICO))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        assertThrows(
                SistemaDuenoRechazoException.class,
                () -> adapter.buscarPorClave(UUID.fromString(CLAVE_MEDICO)));
        mockServer.verify();
    }

    @Test
    void debeListarTodasLasPolizas() {
        mockServer.expect(requestTo(URL_BASE + "/polizas"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[" + POLIZA_MEDICO + "," + POLIZA_AUTO + "]",
                        MediaType.APPLICATION_JSON));

        List<Poliza> polizas = adapter.listarTodas();

        assertEquals(2, polizas.size());
        assertEquals(UUID.fromString(CLAVE_MEDICO), polizas.get(0).getClave());
        assertEquals(UUID.fromString(CLAVE_AUTO), polizas.get(1).getClave());
        mockServer.verify();
    }

    @Test
    void debeDevolverListaVaciaCuandoElServidorRespondeArregloVacio() {
        mockServer.expect(requestTo(URL_BASE + "/polizas"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertTrue(adapter.listarTodas().isEmpty());
        mockServer.verify();
    }

    @Test
    void debeOmitirFilasBasuraYDevolverSoloLasPolizasValidas() {
        String json = "["
                + POLIZA_MEDICO + ","
                + "{\"clave\":\"no-es-uuid\",\"tipo\":1,\"monto\":10.0,"
                + "\"descripcion\":\"x\",\"curpCliente\":\"abc\"},"
                + "{\"clave\":\"7c0150c1-0570-4c09-8548-ca7dddbe12f3\",\"tipo\":7,"
                + "\"monto\":10.0,\"descripcion\":\"x\",\"curpCliente\":\"abc\"},"
                + "{\"clave\":\"6feb8cd9-926b-4c40-8b99-dd0c0466e17c\",\"tipo\":1,"
                + "\"monto\":\"mucho\",\"descripcion\":\"x\",\"curpCliente\":\"abc\"},"
                + "null,"
                + POLIZA_AUTO
                + "]";
        mockServer.expect(requestTo(URL_BASE + "/polizas"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<Poliza> polizas = adapter.listarTodas();

        assertEquals(2, polizas.size());
        assertEquals(UUID.fromString(CLAVE_MEDICO), polizas.get(0).getClave());
        assertEquals(UUID.fromString(CLAVE_AUTO), polizas.get(1).getClave());
        mockServer.verify();
    }

    @Test
    void debeAceptarPolizaSinDescripcion() {
        String json = "[{\"clave\":\"" + CLAVE_AUTO + "\",\"tipo\":0,\"monto\":5,"
                + "\"descripcion\":null,\"curpCliente\":\"broe550101hdfabc00\"}]";
        mockServer.expect(requestTo(URL_BASE + "/polizas"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<Poliza> polizas = adapter.listarTodas();

        assertEquals(1, polizas.size());
        assertNull(polizas.get(0).getDescripcion());
        mockServer.verify();
    }

    @Test
    void debeConsultarPorCurpEnviandolaTalCualSinNormalizar() {
        mockServer.expect(requestTo(URL_BASE + "/polizas/broe550101hdfabc00"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[" + POLIZA_MEDICO + "," + POLIZA_AUTO + "]",
                        MediaType.APPLICATION_JSON));

        List<Poliza> polizas = adapter.buscarPorCurp("broe550101hdfabc00");

        assertEquals(2, polizas.size());
        mockServer.verify();
    }

    @Test
    void debeDevolverListaVaciaParaCurpEnMayusculasSinResultados() {
        mockServer.expect(requestTo(URL_BASE + "/polizas/BROE550101HDFABC00"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertTrue(adapter.buscarPorCurp("BROE550101HDFABC00").isEmpty());
        mockServer.verify();
    }

    @Test
    void debeConsultarPorTipo() {
        mockServer.expect(requestTo(URL_BASE + "/polizas/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[" + POLIZA_MEDICO + "]", MediaType.APPLICATION_JSON));

        List<Poliza> polizas = adapter.buscarPorTipo(2);

        assertEquals(1, polizas.size());
        assertEquals((byte) 2, polizas.get(0).getTipo());
        mockServer.verify();
    }

    @Test
    void debeRechazarTipoFueraDeRangoAntesDeLlamarAlServidor() {
        assertThrows(IllegalArgumentException.class, () -> adapter.buscarPorTipo(3));
        assertThrows(IllegalArgumentException.class, () -> adapter.buscarPorTipo(-1));
        mockServer.verify();
    }

    @Test
    void debeBuscarPorClienteConSegundoApellido() {
        mockServer.expect(requestTo(URL_BASE + "/polizas/c/Emmett%20Lathrop/Brown/Lopez"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[" + POLIZA_AUTO + "]", MediaType.APPLICATION_JSON));

        List<Poliza> polizas = adapter.buscarPorCliente(
                "Emmett Lathrop", "Brown", "Lopez");

        assertEquals(1, polizas.size());
        mockServer.verify();
    }

    @Test
    void debeConvertirTimestampUtcMenosSeisALocalDateConFechasSistemaDueno() {
        assertEquals(
                LocalDate.of(1990, 1, 1),
                FechasSistemaDueno.convertirALocalDate("1990-01-01T06:00:00.000+00:00"));
    }

    @Test
    void debeBuscarPorClienteSinSegundoApellidoOmitiendoSegmentoFinal() {
        mockServer.expect(requestTo(URL_BASE + "/polizas/c/Emmett%20Lathrop/Brown"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertTrue(adapter.buscarPorCliente("Emmett Lathrop", "Brown", null).isEmpty());
        mockServer.verify();
    }

    @Test
    void debeBuscarPorBeneficiarioCodificandoAcentos() {
        mockServer.expect(requestTo(URL_BASE + "/polizas/b/Jos%C3%A9/P%C3%A9rez"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        String nombres = "Jos" + E_ACENTO;
        String primerApellido = "P" + E_ACENTO + "rez";
        assertTrue(adapter.buscarPorBeneficiario(nombres, primerApellido, "  ").isEmpty());
        mockServer.verify();
    }

    @Test
    void debeRechazarSegmentosConDiagonalesAntesDeLlamarAlServidor() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.buscarPorCurp("BROE550101/DFABC00"));
        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.buscarPorCliente("Juan\\", "Perez", null));
        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.buscarPorBeneficiario("Juan", "Perez", "Lo/pez"));
        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.buscarPorCliente(" ", "Perez", null));
        mockServer.verify();
    }

    @Test
    void debeTraducirError500ASistemaDuenoRechazoException() {
        mockServer.expect(requestTo(URL_BASE + "/polizas"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(SistemaDuenoRechazoException.class, () -> adapter.listarTodas());
        mockServer.verify();
    }

    @Test
    void debeTraducirError503ASistemaDuenoNoDisponibleException() {
        mockServer.expect(requestTo(URL_BASE + "/poliza/" + CLAVE_MEDICO))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(
                SistemaDuenoNoDisponibleException.class,
                () -> adapter.buscarPorClave(UUID.fromString(CLAVE_MEDICO)));
        mockServer.verify();
    }

    @Test
    void debeTraducirTimeoutASistemaDuenoNoDisponibleException() {
        mockServer.expect(requestTo(URL_BASE + "/polizas/1"))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThrows(SistemaDuenoNoDisponibleException.class, () -> adapter.buscarPorTipo(1));
        mockServer.verify();
    }
}
