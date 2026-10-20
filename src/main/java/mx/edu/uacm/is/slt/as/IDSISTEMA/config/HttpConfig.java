package mx.edu.uacm.is.slt.as.IDSISTEMA.config;


import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

public class HttpConfig {
    @Bean
    public RestClient.Builder(){
            .baseUrl("https://api.ejemplo.com")
            .defaulHeader("Content-Type","application/json")
            .build();
}
@Bean
public WebClient webClient() {
    return WebClient.builder()
            .baseUrl("https://api.ejemplo.com")
            .defaulHeader("Content-Type", "application/json")
            .build();
    }
}