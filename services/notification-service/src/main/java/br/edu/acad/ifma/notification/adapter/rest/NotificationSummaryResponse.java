package br.edu.acad.ifma.notification.adapter.rest;

import br.edu.acad.ifma.notification.domain.NotificationStatus;
import br.edu.acad.ifma.notification.domain.PushNotification;
import java.time.Instant;

public record NotificationSummaryResponse(
        Long id,
        String title,
        NotificationStatus status,
        Instant createdAt
) {
    public static NotificationSummaryResponse from(PushNotification n) {
        return new NotificationSummaryResponse(
                n.getId(),
                n.getTitle().value(),
                n.getStatus(),
                n.getCreatedAt()
        );
    }
}
