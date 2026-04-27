package br.edu.acad.ifma.adapters.api.rest.outbound;

import br.edu.acad.ifma.app.domain.notification.NotificationStatus;
import java.time.Instant;

public class NotificationSummaryResponse {

    private Long id;
    private String title;
    private NotificationStatus status;
    private String recipientToken;
    private String fcmMessageId;
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getRecipientToken() {
        return recipientToken;
    }

    public void setRecipientToken(String recipientToken) {
        this.recipientToken = recipientToken;
    }

    public String getFcmMessageId() {
        return fcmMessageId;
    }

    public void setFcmMessageId(String fcmMessageId) {
        this.fcmMessageId = fcmMessageId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
