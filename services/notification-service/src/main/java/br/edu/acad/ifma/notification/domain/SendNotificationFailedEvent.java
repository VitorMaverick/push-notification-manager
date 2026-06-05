package br.edu.acad.ifma.notification.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * Domain event emitted when a push notification attempt fails.
 * Lives in the domain package so that port and usecase layers can depend on it
 * without crossing into adapter territory.
 */
public class SendNotificationFailedEvent implements Serializable {

    private Long notificationId;
    private String recipientToken;
    private String title;
    private String body;
    private Map<String, String> data;
    private String failureReason;
    private int attemptCount;
    private Instant failedAt;

    public SendNotificationFailedEvent() {}

    public SendNotificationFailedEvent(
            Long notificationId, String recipientToken, String title, String body,
            Map<String, String> data, String failureReason, int attemptCount) {
        this.notificationId = notificationId;
        this.recipientToken = recipientToken;
        this.title = title;
        this.body = body;
        this.data = data;
        this.failureReason = failureReason;
        this.attemptCount = attemptCount;
        this.failedAt = Instant.now();
    }

    public Long getNotificationId() { return notificationId; }
    public String getRecipientToken() { return recipientToken; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public Map<String, String> getData() { return data; }
    public String getFailureReason() { return failureReason; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getFailedAt() { return failedAt; }

    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    public void setRecipientToken(String recipientToken) { this.recipientToken = recipientToken; }
    public void setTitle(String title) { this.title = title; }
    public void setBody(String body) { this.body = body; }
    public void setData(Map<String, String> data) { this.data = data; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public void setFailedAt(Instant failedAt) { this.failedAt = failedAt; }

    public SendNotificationFailedEvent withIncrementedAttempt(String newReason) {
        return new SendNotificationFailedEvent(
                notificationId, recipientToken, title, body, data, newReason, attemptCount + 1);
    }
}
