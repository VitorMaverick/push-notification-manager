package br.edu.acad.ifma.notification.adapter.rest;

import br.edu.acad.ifma.notification.domain.NotificationStatus;
import br.edu.acad.ifma.notification.domain.PushNotification;
import java.time.Instant;

public record NotificationResponse(
        Long id,
        String title,
        String body,
        String recipientToken,
        NotificationStatus status,
        String fcmMessageId,
        Instant sentAt,
        Instant deliveredAt,
        Instant createdAt
) {
    public static NotificationResponse from(PushNotification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle().value(),
                n.getBody().value(),
                n.getRecipientToken().value(),
                n.getStatus(),
                n.getFcmMessageId(),
                n.getSentAt(),
                n.getDeliveredAt(),
                n.getCreatedAt()
        );
    }
}
