package br.edu.acad.ifma.notification.adapter.rest;

import jakarta.validation.constraints.NotBlank;

public record FcmAckRequest(@NotBlank String fcmMessageId) {}
