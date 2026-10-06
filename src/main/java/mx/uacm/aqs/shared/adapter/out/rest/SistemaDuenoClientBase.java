package mx.uacm.aqs.shared.adapter.out.rest;

import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

public class SistemaDuenoClientBase {

    private final RestClient restClient;
    private final WebClient webClient;

    public SistemaDuenoClientBase(RestClient restClient, WebClient webClient) {
        this.restClient = restClient;
        this.webClient = webClient;
    }

    protected RestClient restClient() {
        return restClient;
    }

    protected WebClient webClient() {
        return webClient;
    }
}
