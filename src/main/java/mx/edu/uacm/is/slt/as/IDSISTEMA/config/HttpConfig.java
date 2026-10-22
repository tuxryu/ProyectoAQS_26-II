package mx.edu.uacm.is.slt.as.IDSISTEMA.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuracion del cliente HTTP y timeouts hacia el Sistema Dueno.
 */
@Configuration
public class HttpConfig {

    @Bean
    public RestClient sistemaDuenoRestClient(
            @Value("${sistema-dueno.url-base}") String urlBase,
            @Value("${sistema-dueno.timeouts.conexion}") Duration timeoutConexion,
            @Value("${sistema-dueno.timeouts.lectura}") Duration timeoutLectura) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutConexion);
        requestFactory.setReadTimeout(timeoutLectura);

        return RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
