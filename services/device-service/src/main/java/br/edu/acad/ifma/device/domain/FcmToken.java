package br.edu.acad.ifma.device.domain;

public record FcmToken(String value) {

    public FcmToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FCM token must not be blank");
        }
    }
}
