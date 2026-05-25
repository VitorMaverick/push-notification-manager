package br.edu.acad.ifma.adapters.inbound;

import java.time.Instant;

/**
 * Inbound DTO representing an ACK from the client/service-worker to signal delivery of a sent FCM message.
 */
public class FcmAckRequest {

    private Long notificationId;
    private String messageId;
    private String token;
    private Instant receivedAt;

    public FcmAckRequest() {}

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}
