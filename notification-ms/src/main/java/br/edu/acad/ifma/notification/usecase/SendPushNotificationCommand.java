package br.edu.acad.ifma.notification.usecase;

import java.util.Map;

public record SendPushNotificationCommand(
        String recipientToken,
        String title,
        String body,
        Map<String, String> data
) {}
