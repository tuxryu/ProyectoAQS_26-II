package mx.uacm.aqs.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sistema-dueno")
public record SistemaDuenoProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
