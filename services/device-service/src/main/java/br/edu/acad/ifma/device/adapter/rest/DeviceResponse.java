package br.edu.acad.ifma.device.adapter.rest;

import br.edu.acad.ifma.device.domain.DeviceStatus;
import br.edu.acad.ifma.device.domain.DeviceType;
import java.time.Instant;

public record DeviceResponse(
    Long id,
    String fcmToken,
    DeviceType platform,
    String userAgent,
    DeviceStatus status,
    Instant registeredAt,
    Instant lastUsedAt
) {}
