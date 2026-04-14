package br.edu.acad.ifma.app.usecase.notification;

import br.edu.acad.ifma.app.domain.notification.NotificationStatus;
import java.time.Instant;

public class NotificationFilter {

    private NotificationStatus status;
    private String deviceToken;
    private Instant fromDate;
    private Instant toDate;

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public Instant getFromDate() {
        return fromDate;
    }

    public void setFromDate(Instant fromDate) {
        this.fromDate = fromDate;
    }

    public Instant getToDate() {
        return toDate;
    }

    public void setToDate(Instant toDate) {
        this.toDate = toDate;
    }
}
