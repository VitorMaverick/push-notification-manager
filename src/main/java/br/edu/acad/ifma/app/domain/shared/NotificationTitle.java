package br.edu.acad.ifma.app.domain.shared;

import br.edu.acad.ifma.app.domain.shared.exception.DomainException;

public final class NotificationTitle {

    private final String value;

    public NotificationTitle(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("Notification title must not be blank");
        }
        if (value.length() > 255) {
            throw new DomainException("Notification title must not exceed 255 characters");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationTitle)) return false;
        NotificationTitle that = (NotificationTitle) o;
        return value.equals(that.value);
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
