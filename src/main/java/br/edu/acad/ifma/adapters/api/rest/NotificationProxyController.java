package br.edu.acad.ifma.adapters.api.rest;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Transparent proxy that forwards all /api/v1/notifications/** requests to the
 * notification-service running on a separate port.
 *
 * <p>This controller exists because the monolith acts as the entry point for the
 * frontend (port 8080) and must route notification traffic to the microservice
 * (port 8082) without duplicating business logic.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationProxyController {

    private static final Logger log = LoggerFactory.getLogger(NotificationProxyController.class);

    private static final List<String> HOP_BY_HOP_HEADERS = List.of(
        HttpHeaders.CONNECTION,
        "Keep-Alive",
        HttpHeaders.PROXY_AUTHENTICATE,
        HttpHeaders.PROXY_AUTHORIZATION,
        HttpHeaders.TE,
        HttpHeaders.TRAILER,
        HttpHeaders.TRANSFER_ENCODING,
        HttpHeaders.UPGRADE
    );

    private final RestTemplate restTemplate;
    private final String notificationServiceBaseUrl;

    public NotificationProxyController(
        RestTemplate restTemplate,
        @Value("${notification-service.base-url:http://localhost:8082}") String notificationServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
    }

    @RequestMapping(value = { "", "/**" })
    public ResponseEntity<byte[]> proxy(RequestEntity<byte[]> inbound, HttpServletRequest servletRequest) {
        String requestPath = servletRequest.getRequestURI();
        String query = servletRequest.getQueryString();

        URI targetUri = UriComponentsBuilder.fromUriString(notificationServiceBaseUrl)
            .replacePath(requestPath)
            .replaceQuery(query)
            .build(true)
            .toUri();

        log.debug("Proxying {} {} -> {}", inbound.getMethod(), requestPath, targetUri);

        HttpHeaders forwardHeaders = filterHopByHopHeaders(inbound.getHeaders());

        RequestEntity<byte[]> outbound = new RequestEntity<>(
            inbound.getBody(),
            forwardHeaders,
            inbound.getMethod() != null ? inbound.getMethod() : HttpMethod.GET,
            targetUri
        );

        return restTemplate.exchange(outbound, byte[].class);
    }

    private HttpHeaders filterHopByHopHeaders(HttpHeaders original) {
        HttpHeaders filtered = new HttpHeaders();
        original.forEach((name, values) -> {
            if (!HOP_BY_HOP_HEADERS.contains(name)) {
                filtered.addAll(name, values);
            }
        });
        return filtered;
    }
}
