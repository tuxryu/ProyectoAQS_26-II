package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest;

import java.net.SocketTimeoutException;
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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Pruebas unitarias de adaptadores con MockRestServiceServer para
 * ClienteSistemaDuenoAdapter y FechasSistemaDueno.
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

        Optional<ClienteRemotoDto> resultado = adapter.buscarPorCurp("broe550101hdfabc00");

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
    void debeNormalizarSegundoApellidoVacioANullEnLectura() {
        String json = """
                {
                  "curp": "T404900101HDFAAA01",
                  "nombres": "Juan",
                  "primerApellido": "Perez",
                  "segundoApellido": "   ",
                  "direccion": "Calle 1",
                  "fechaNacimiento": "1990-01-01T06:00:00.000+00:00"
                }
                """;

        mockServer.expect(requestTo(URL_BASE + "/cliente/T404900101HDFAAA01"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Optional<ClienteRemotoDto> resultado = adapter.buscarPorCurp("T404900101HDFAAA01");

        assertTrue(resultado.isPresent());
        assertNull(resultado.get().getSegundoApellido());
        mockServer.verify();
    }

    @Test
    void debeParsearFechaUtcMenosSeisEnHorarioEstandarYVerano() {
        assertEquals(
                LocalDate.of(1990, 1, 1),
                FechasSistemaDueno.convertirALocalDate("1990-01-01T06:00:00.000+00:00"));
        assertEquals(
                LocalDate.of(2003, 7, 15),
                FechasSistemaDueno.convertirALocalDate("2003-07-15T05:00:00.000+00:00"));
        assertEquals(
                "1990-01-01",
                FechasSistemaDueno.formatearParaRuta(LocalDate.of(1990, 1, 1)));
    }

    @Test
    void debeDevolverOptionalEmptyCuandoSistemaDuenoResponde200Vacio() {
        mockServer.expect(requestTo(URL_BASE + "/cliente/BROE550101HDFABC00"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        Optional<ClienteRemotoDto> resultado = adapter.buscarPorCurp("BROE550101HDFABC00");

        assertTrue(resultado.isEmpty());
        mockServer.verify();
    }

    @Test
    void debeGuardarClienteConSegundoApellidoMediantePost() {
        String json = """
                {
                  "curp": "T404900101HDFAAA01",
                  "nombres": "Juan",
                  "primerApellido": "Perez",
                  "segundoApellido": "Lopez",
                  "direccion": "Calle10",
                  "fechaNacimiento": "1990-01-01T06:00:00.000+00:00"
                }
                """;
        String rutaEsperada = URL_BASE
                + "/cliente/T404900101HDFAAA01/Calle10/1990-01-01/Juan/Perez/Lopez";

        mockServer.expect(requestTo(rutaEsperada))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        ClienteRemotoDto peticion = new ClienteRemotoDto(
                "T404900101HDFAAA01",
                "Juan",
                "Perez",
                "Lopez",
                "Calle10",
                "1990-01-01");

        ClienteRemotoDto guardado = adapter.guardar(peticion);

        assertEquals("T404900101HDFAAA01", guardado.getCurp());
        assertEquals("Lopez", guardado.getSegundoApellido());
        assertEquals(LocalDate.of(1990, 1, 1), guardado.obtenerFechaNacimientoLocal());
        mockServer.verify();
    }

    @Test
    void debeGuardarClienteSinSegundoApellidoOmitiendoSegmentoFinal() {
        String json = """
                {
                  "curp": "T404900101HDFAAA01",
                  "nombres": "Juan",
                  "primerApellido": "Perez",
                  "segundoApellido": null,
                  "direccion": "Calle10",
                  "fechaNacimiento": "1990-01-01T06:00:00.000+00:00"
                }
                """;
        String rutaEsperada = URL_BASE
                + "/cliente/T404900101HDFAAA01/Calle10/1990-01-01/Juan/Perez";

        mockServer.expect(requestTo(rutaEsperada))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        ClienteRemotoDto peticion = new ClienteRemotoDto(
                "T404900101HDFAAA01",
                "Juan",
                "Perez",
                null,
                "Calle10",
                "1990-01-01");

        ClienteRemotoDto guardado = adapter.guardar(peticion);

        assertNull(guardado.getSegundoApellido());
        mockServer.verify();
    }

    @Test
    void debeActualizarClienteConSegundoApellidoMediantePut() {
        String json = """
                {
                  "curp": "T404900101HDFAAA01",
                  "nombres": "Juan",
                  "primerApellido": "Perez",
                  "segundoApellido": "Gomez",
                  "direccion": "CallePut5",
                  "fechaNacimiento": "1990-01-01T06:00:00.000+00:00"
                }
                """;
        String rutaEsperada = URL_BASE
                + "/cliente/T404900101HDFAAA01/CallePut5/1990-01-01/Juan/Perez/Gomez";

        mockServer.expect(requestTo(rutaEsperada))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        ClienteRemotoDto peticion = new ClienteRemotoDto(
                "T404900101HDFAAA01",
                "Juan",
                "Perez",
                "Gomez",
                "CallePut5",
                "1990-01-01");

        ClienteRemotoDto actualizado = adapter.actualizar("T404900101HDFAAA01", peticion);

        assertEquals("CallePut5", actualizado.getDireccion());
        assertEquals("Gomez", actualizado.getSegundoApellido());
        mockServer.verify();
    }

    @Test
    void debeActualizarClienteSinSegundoApellidoMediantePostPorHallazgoOcho() {
        String json = """
                {
                  "curp": "T404900101HDFAAA01",
                  "nombres": "Juan",
                  "primerApellido": "Perez",
                  "segundoApellido": null,
                  "direccion": "CallePut6",
                  "fechaNacimiento": "1990-01-01T06:00:00.000+00:00"
                }
                """;
        String rutaEsperada = URL_BASE
                + "/cliente/T404900101HDFAAA01/CallePut6/1990-01-01/Juan/Perez";

        mockServer.expect(requestTo(rutaEsperada))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        ClienteRemotoDto peticion = new ClienteRemotoDto(
                "T404900101HDFAAA01",
                "Juan",
                "Perez",
                null,
                "CallePut6",
                "1990-01-01");

        ClienteRemotoDto actualizado = adapter.actualizar("T404900101HDFAAA01", peticion);

        assertEquals("CallePut6", actualizado.getDireccion());
        assertNull(actualizado.getSegundoApellido());
        mockServer.verify();
    }

    @Test
    void debeLanzarRechazoCuandoEscrituraResponde200Vacio() {
        String rutaEsperada = URL_BASE
                + "/cliente/T404900101HDFAAA01/Calle10/1990-01-01/Juan/Perez";

        mockServer.expect(requestTo(rutaEsperada))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        ClienteRemotoDto peticion = new ClienteRemotoDto(
                "T404900101HDFAAA01",
                "Juan",
                "Perez",
                null,
                "Calle10",
                "1990-01-01");

        assertThrows(
                SistemaDuenoRechazoException.class,
                () -> adapter.guardar(peticion));
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
    void debeTraducirFalloDeRedOTimeoutASistemaDuenoNoDisponibleException() {
        mockServer.expect(requestTo(URL_BASE + "/cliente/T404900101HDFAAA01"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

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
