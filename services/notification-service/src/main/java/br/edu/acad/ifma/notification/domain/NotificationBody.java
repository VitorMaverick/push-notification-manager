package br.edu.acad.ifma.notification.domain;

public record NotificationBody(String value) {
    public NotificationBody {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Notification body must not be blank");
    }
}
