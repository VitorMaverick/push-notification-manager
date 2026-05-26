package br.edu.acad.ifma.notification.domain;

public record NotificationTitle(String value) {
    public NotificationTitle {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Notification title must not be blank");
    }
}
