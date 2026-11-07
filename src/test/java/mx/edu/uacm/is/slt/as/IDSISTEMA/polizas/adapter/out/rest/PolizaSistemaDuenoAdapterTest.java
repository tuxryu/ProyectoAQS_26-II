package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.out.rest;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PolizaSistemaDuenoAdapterTest {

    private MockRestServiceServer mockServer;
    private PolizaSistemaDuenoAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        adapter = new PolizaSistemaDuenoAdapter(builder.build());
    }

    @Test
    void debeGuardarPolizaCorrectamenteMediantePost() {
        UUID clave = UUID.randomUUID();
        Poliza poliza = new Poliza(
                clave, (byte) 1, 200000.0f, "Poliza Auto", "BROE550101HDFABC00");

        String jsonRespuesta = "{\"clave\":\"" + clave + "\",\"tipo\":1,\"monto\":200000.0,"
                + "\"descripcion\":\"Poliza Auto\",\"curpCliente\":\"BROE550101HDFABC00\"}";

        mockServer.expect(requestTo("/polizas"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jsonRespuesta));

        Poliza resultado = adapter.guardar(poliza);
        assertNotNull(resultado);
        mockServer.verify();
    }

    @Test
    void debeActualizarPolizaCorrectamenteMediantePut() {
        UUID clave = UUID.randomUUID();
        Poliza poliza = new Poliza(
                clave, (byte) 1, 250000.0f, "Poliza Editada", "BROE550101HDFABC00");

        mockServer.expect(requestTo("/poliza/" + clave))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> adapter.actualizar(clave, poliza));
        mockServer.verify();
    }

    @Test
    void debeEliminarPolizaCorrectamenteMedianteDelete() {
        UUID clave = UUID.randomUUID();

        mockServer.expect(requestTo("/poliza/" + clave))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertDoesNotThrow(() -> adapter.eliminar(clave));
        mockServer.verify();
    }
}