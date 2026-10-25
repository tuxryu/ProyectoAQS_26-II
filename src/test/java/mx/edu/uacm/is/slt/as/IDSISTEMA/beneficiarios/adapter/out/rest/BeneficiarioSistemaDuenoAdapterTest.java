
package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.out.rest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BeneficiarioSistemaDuenoAdapterTest {

    private static final String URL_BASE = "http://nachintoch.mx:8080";
    private static final String CLAVE
            = "1c79ca05-4cc1-46ab-8f67-7d18d6959ac3";

    private MockRestServiceServer mockServer;
    private BeneficiarioSistemaDuenoAdapter adapter;

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(URL_BASE);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        adapter = new BeneficiarioSistemaDuenoAdapter(builder.build());
    }

    @Test
    void debeConsultarBeneficiarioConSegundoApellido() {
        String json = """
                {
                  "id": {
                    "nombres": "Mario",
                    "primerApellido": "Prueba",
                    "segundoApellido": "Lopez",
                    "fechaNacimiento": "2000-02-02T06:00:00.000+00:00",
                    "clavePoliza": "%s"
                  },
                  "porcentaje": 60
                }
                """.formatted(CLAVE);

        mockServer.expect(requestTo(
                        URL_BASE + "/beneficiario/2000-02-02/" + CLAVE
                                + "/Mario/Prueba/Lopez"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Optional<Beneficiario> resultado = adapter.buscarPorDatos(
                LocalDate.of(2000, 2, 2),
                UUID.fromString(CLAVE),
                "Mario",
                "Prueba",
                "Lopez");

        assertTrue(resultado.isPresent());
        Beneficiario beneficiario = resultado.orElseThrow();

        assertEquals("Mario", beneficiario.getNombres());
        assertEquals("Prueba", beneficiario.getPrimerApellido());
        assertEquals("Lopez", beneficiario.getSegundoApellido());
        assertEquals(LocalDate.of(2000, 2, 2),
                beneficiario.getFechaNacimiento());
        assertEquals(UUID.fromString(CLAVE), beneficiario.getClavePoliza());
        assertEquals(60, beneficiario.getPorcentaje());

        mockServer.verify();
    }

    @Test
    void debeDevolverVacioCuandoLaRespuestaNoTieneCuerpo() {
        mockServer.expect(requestTo(
                        URL_BASE + "/beneficiario/2000-02-02/" + CLAVE
                                + "/Mario/Prueba"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        Optional<Beneficiario> resultado = adapter.buscarPorDatos(
                LocalDate.of(2000, 2, 2),
                UUID.fromString(CLAVE),
                "Mario",
                "Prueba",
                null);

        assertTrue(resultado.isEmpty());
        mockServer.verify();
    }

    @Test
    void debeTraducirError503ComoServicioNoDisponible() {
        mockServer.expect(requestTo(
                        URL_BASE + "/beneficiario/2000-02-02/" + CLAVE
                                + "/Mario/Prueba"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(
                SistemaDuenoNoDisponibleException.class,
                () -> adapter.buscarPorDatos(
                        LocalDate.of(2000, 2, 2),
                        UUID.fromString(CLAVE),
                        "Mario",
                        "Prueba",
                        null));

        mockServer.verify();
    }

    @Test
    void debeTraducirError500ComoRechazoDelSistemaDueno() {
        mockServer.expect(requestTo(
                        URL_BASE + "/beneficiario/2000-02-02/" + CLAVE
                                + "/Mario/Prueba"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(
                SistemaDuenoRechazoException.class,
                () -> adapter.buscarPorDatos(
                        LocalDate.of(2000, 2, 2),
                        UUID.fromString(CLAVE),
                        "Mario",
                        "Prueba",
                        null));

        mockServer.verify();
    }
}
