package br.edu.acad.ifma.app.domain.notification;

import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.NotificationBody;
import br.edu.acad.ifma.app.domain.shared.NotificationTitle;
import java.time.Instant;

public class PushNotification {

    private Long id;
    private NotificationTitle title;
    private NotificationBody body;
    private FcmToken recipientToken;
    private NotificationStatus status;
    private String fcmMessageId;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant createdAt;

    private PushNotification() {}

    public void markSent(String fcmMessageId) {
        this.status = NotificationStatus.SENT;
        this.fcmMessageId = fcmMessageId;
        this.sentAt = Instant.now();
    }

    public void markDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.fcmMessageId = reason;
    }

    public Long getId() {
        return id;
    }

    public NotificationTitle getTitle() {
        return title;
    }

    public NotificationBody getBody() {
        return body;
    }

    public FcmToken getRecipientToken() {
        return recipientToken;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getFcmMessageId() {
        return fcmMessageId;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static PushNotificationBuilder builder() {
        return new PushNotificationBuilder();
    }

    public static final class PushNotificationBuilder {

        private Long id;
        private NotificationTitle title;
        private NotificationBody body;
        private FcmToken recipientToken;
        private NotificationStatus status;
        private String fcmMessageId;
        private Instant sentAt;
        private Instant deliveredAt;
        private Instant createdAt;

        private PushNotificationBuilder() {}

        public PushNotificationBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public PushNotificationBuilder withTitle(NotificationTitle title) {
            this.title = title;
            return this;
        }

        public PushNotificationBuilder withBody(NotificationBody body) {
            this.body = body;
            return this;
        }

        public PushNotificationBuilder withRecipientToken(FcmToken recipientToken) {
            this.recipientToken = recipientToken;
            return this;
        }

        public PushNotificationBuilder withStatus(NotificationStatus status) {
            this.status = status;
            return this;
        }

        public PushNotificationBuilder withFcmMessageId(String fcmMessageId) {
            this.fcmMessageId = fcmMessageId;
            return this;
        }

        public PushNotificationBuilder withSentAt(Instant sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public PushNotificationBuilder withDeliveredAt(Instant deliveredAt) {
            this.deliveredAt = deliveredAt;
            return this;
        }

        public PushNotificationBuilder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PushNotification build() {
            PushNotification notification = new PushNotification();
            notification.id = this.id;
            notification.title = this.title;
            notification.body = this.body;
            notification.recipientToken = this.recipientToken;
            notification.status = this.status;
            notification.fcmMessageId = this.fcmMessageId;
            notification.sentAt = this.sentAt;
            notification.deliveredAt = this.deliveredAt;
            notification.createdAt = this.createdAt;
            return notification;
        }
    }
}
