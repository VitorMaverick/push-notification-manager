package br.edu.acad.ifma.notification.domain;

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

    public Long getId() { return id; }
    public NotificationTitle getTitle() { return title; }
    public NotificationBody getBody() { return body; }
    public FcmToken getRecipientToken() { return recipientToken; }
    public NotificationStatus getStatus() { return status; }
    public String getFcmMessageId() { return fcmMessageId; }
    public Instant getSentAt() { return sentAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public Instant getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long id;
        private NotificationTitle title;
        private NotificationBody body;
        private FcmToken recipientToken;
        private NotificationStatus status;
        private String fcmMessageId;
        private Instant sentAt;
        private Instant deliveredAt;
        private Instant createdAt;

        private Builder() {}

        public Builder withId(Long id) { this.id = id; return this; }
        public Builder withTitle(NotificationTitle title) { this.title = title; return this; }
        public Builder withBody(NotificationBody body) { this.body = body; return this; }
        public Builder withRecipientToken(FcmToken recipientToken) { this.recipientToken = recipientToken; return this; }
        public Builder withStatus(NotificationStatus status) { this.status = status; return this; }
        public Builder withFcmMessageId(String fcmMessageId) { this.fcmMessageId = fcmMessageId; return this; }
        public Builder withSentAt(Instant sentAt) { this.sentAt = sentAt; return this; }
        public Builder withDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; return this; }
        public Builder withCreatedAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public PushNotification build() {
            PushNotification n = new PushNotification();
            n.id = this.id; n.title = this.title; n.body = this.body;
            n.recipientToken = this.recipientToken; n.status = this.status;
            n.fcmMessageId = this.fcmMessageId; n.sentAt = this.sentAt;
            n.deliveredAt = this.deliveredAt; n.createdAt = this.createdAt;
            return n;
        }
    }
}
