package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest.dto.ClienteRemotoDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http.FechasSistemaDueno;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Pruebas unitarias para ClienteSistemaDuenoAdapter y FechasSistemaDueno.
 */
class ClienteSistemaDuenoAdapterTest {

    private static final String URL_BASE = "http://nachintoch.mx:8080";

    private MockRestServiceServer mockServer;
    private ClienteSistemaDuenoAdapter adapter;

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(URL_BASE);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        adapter = new ClienteSistemaDuenoAdapter(builder.build());
    }

    @Test
    void debeConsultarClienteSembradoYParsearFechaHistoricaCdmx() {
        String json = """
                {
                  "curp": "broe550101hdfabc00",
                  "nombres": "Roberto",
                  "primerApellido": "Bolanios",
                  "segundoApellido": null,
                  "direccion": "Vecindad 8",
                  "fechaNacimiento": "1920-01-01T06:36:36.000+00:00"
                }
                """;

        mockServer.expect(requestTo(URL_BASE + "/cliente/broe550101hdfabc00"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Optional<ClienteRemotoDto> resultado =
                adapter.buscarPorCurp("broe550101hdfabc00");

        assertTrue(resultado.isPresent());
        ClienteRemotoDto dto = resultado.get();
        assertEquals("broe550101hdfabc00", dto.getCurp());
        assertEquals("Roberto", dto.getNombres());
        assertEquals("Bolanios", dto.getPrimerApellido());
        assertNull(dto.getSegundoApellido());
        assertEquals(LocalDate.of(1920, 1, 1), dto.obtenerFechaNacimientoLocal());
        mockServer.verify();
    }

    @Test
    void debeParsearFechaUtcMenosSeisEnHorarioEstandarYVerano() {
        assertEquals(
                LocalDate.of(1990, 1, 1),
                FechasSistemaDueno.aLocalDate("1990-01-01T06:00:00.000+00:00"));
        assertEquals(
                LocalDate.of(2003, 7, 15),
                FechasSistemaDueno.aLocalDate("2003-07-15T05:00:00.000+00:00"));
        assertEquals(
                "1990-01-01",
                FechasSistemaDueno.formatearParaRuta(LocalDate.of(1990, 1, 1)));
    }

    @Test
    void debeDevolverOptionalEmptyCuandoSistemaDuenoResponde200Vacio() {
        mockServer.expect(requestTo(URL_BASE + "/cliente/BROE550101HDFABC00"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        Optional<ClienteRemotoDto> resultado =
                adapter.buscarPorCurp("BROE550101HDFABC00");

        assertTrue(resultado.isEmpty());
        mockServer.verify();
    }

    @Test
    void debeTraducirError503ASistemaDuenoNoDisponibleException() {
        mockServer.expect(requestTo(URL_BASE + "/cliente/T404900101HDFAAA01"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(
                SistemaDuenoNoDisponibleException.class,
                () -> adapter.buscarPorCurp("T404900101HDFAAA01"));
        mockServer.verify();
    }

    @Test
    void debeTraducirError500ASistemaDuenoRechazoException() {
        mockServer.expect(requestTo(URL_BASE + "/cliente/T404900101HDFAAA01"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(
                SistemaDuenoRechazoException.class,
                () -> adapter.buscarPorCurp("T404900101HDFAAA01"));
        mockServer.verify();
    }

    @Test
    void debeRechazarCurpConDiagonalAntesDeLlamarAlServidor() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.buscarPorCurp("BROE550101/DFABC00"));
    }
}
