package br.edu.acad.ifma.notification.adapter.rest;

import jakarta.validation.constraints.NotNull;

public record FcmAckRequest(@NotNull Long notificationId) {}
