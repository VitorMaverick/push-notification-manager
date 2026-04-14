package br.edu.acad.ifma.adapters.factory;

import br.edu.acad.ifma.adapters.model.NotificationJpaEntity;
import br.edu.acad.ifma.app.domain.notification.PushNotification;
import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.NotificationBody;
import br.edu.acad.ifma.app.domain.shared.NotificationTitle;
import br.edu.acad.ifma.domain.NotificationChannel;

public abstract class NotificationFactory {

    private NotificationFactory() {}

    public static NotificationJpaEntity toEntity(PushNotification domain) {
        NotificationJpaEntity e = new NotificationJpaEntity();
        e.setId(domain.getId());
        e.setTitle(domain.getTitle().value());
        e.setBody(domain.getBody().value());
        e.setRecipientToken(domain.getRecipientToken().value());
        e.setStatus(domain.getStatus());
        e.setFcmMessageId(domain.getFcmMessageId());
        e.setSentAt(domain.getSentAt());
        e.setDeliveredAt(domain.getDeliveredAt());
        e.setCreatedAt(domain.getCreatedAt());
        e.setChannel(NotificationChannel.FCM_PUSH);
        return e;
    }

    public static PushNotification toDomain(NotificationJpaEntity e) {
        return PushNotification.builder()
            .withId(e.getId())
            .withTitle(new NotificationTitle(e.getTitle()))
            .withBody(new NotificationBody(e.getBody()))
            .withRecipientToken(new FcmToken(e.getRecipientToken()))
            .withStatus(e.getStatus())
            .withFcmMessageId(e.getFcmMessageId())
            .withSentAt(e.getSentAt())
            .withDeliveredAt(e.getDeliveredAt())
            .withCreatedAt(e.getCreatedAt())
            .build();
    }
}
