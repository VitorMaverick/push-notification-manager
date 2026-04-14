package br.edu.acad.ifma.app.domain.shared;

import br.edu.acad.ifma.app.domain.shared.exception.InvalidFcmTokenException;

public final class FcmToken {

    private final String value;

    public FcmToken(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidFcmTokenException("FCM token must not be blank");
        }
        if (value.length() < 20) {
            throw new InvalidFcmTokenException("FCM token too short");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FcmToken)) return false;
        FcmToken fcmToken = (FcmToken) o;
        return value.equals(fcmToken.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
