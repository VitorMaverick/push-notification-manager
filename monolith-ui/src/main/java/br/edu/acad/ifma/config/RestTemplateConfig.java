package br.edu.acad.ifma.config;

import java.time.Duration;
import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .connectTimeout(Duration.ofSeconds(3))
            .readTimeout(Duration.ofSeconds(5))
            .interceptors((request, body, execution) -> {
                String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
                if (correlationId != null) {
                    request.getHeaders().add(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
                }
                return execution.execute(request, body);
            })
            .build();
    }
}
