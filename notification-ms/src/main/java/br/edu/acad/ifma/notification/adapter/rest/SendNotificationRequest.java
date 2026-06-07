package br.edu.acad.ifma.notification.adapter.rest;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record SendNotificationRequest(
        @NotBlank String recipientToken,
        @NotBlank String title,
        @NotBlank String body,
        Map<String, String> data
) {}
