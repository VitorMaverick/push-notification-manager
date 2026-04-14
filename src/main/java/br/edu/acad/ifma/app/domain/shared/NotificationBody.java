package br.edu.acad.ifma.app.domain.shared;

import br.edu.acad.ifma.app.domain.shared.exception.DomainException;

public final class NotificationBody {

    private final String value;

    public NotificationBody(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("Notification body must not be blank");
        }
        if (value.length() > 2000) {
            throw new DomainException("Notification body must not exceed 2000 characters");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationBody)) return false;
        NotificationBody that = (NotificationBody) o;
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
