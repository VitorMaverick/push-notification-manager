package br.edu.acad.ifma.notification.usecase;

import br.edu.acad.ifma.notification.domain.NotificationStatus;

public record NotificationFilter(NotificationStatus status, String fcmToken) {
    public static NotificationFilter empty() {
        return new NotificationFilter(null, null);
    }
}
