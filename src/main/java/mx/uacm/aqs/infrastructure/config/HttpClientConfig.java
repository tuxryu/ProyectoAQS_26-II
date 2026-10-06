package mx.uacm.aqs.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(SistemaDuenoProperties.class)
public class HttpClientConfig {

    @Bean
    public RestClient sistemaDuenoRestClient(SistemaDuenoProperties properties) {
        Duration connectTimeout = properties.connectTimeout();
        Duration readTimeout = properties.readTimeout();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) connectTimeout.toMillis());
        requestFactory.setReadTimeout((int) readTimeout.toMillis());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public WebClient sistemaDuenoWebClient(SistemaDuenoProperties properties) {
        Duration connectTimeout = properties.connectTimeout();
        Duration readTimeout = properties.readTimeout();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis())
                .doOnConnected(connection ->
                        connection.addHandlerLast(
                                new ReadTimeoutHandler(readTimeout.toMillis(), TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(properties.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
