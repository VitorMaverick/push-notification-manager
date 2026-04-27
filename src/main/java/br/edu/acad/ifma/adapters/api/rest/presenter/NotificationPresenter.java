package br.edu.acad.ifma.adapters.api.rest.presenter;

import br.edu.acad.ifma.adapters.api.rest.outbound.NotificationDetailResponse;
import br.edu.acad.ifma.adapters.api.rest.outbound.NotificationResponse;
import br.edu.acad.ifma.adapters.api.rest.outbound.NotificationSummaryResponse;
import br.edu.acad.ifma.app.domain.notification.PushNotification;

public abstract class NotificationPresenter {

    private NotificationPresenter() {}

    public static NotificationResponse toResponse(PushNotification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setStatus(n.getStatus());
        r.setFcmMessageId(n.getFcmMessageId());
        r.setSentAt(n.getSentAt());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }

    public static NotificationSummaryResponse toSummary(PushNotification n) {
        NotificationSummaryResponse r = new NotificationSummaryResponse();
        r.setId(n.getId());
        r.setTitle(n.getTitle().value());
        r.setStatus(n.getStatus());
        r.setRecipientToken(n.getRecipientToken().value());
        r.setFcmMessageId(n.getFcmMessageId());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }

    public static NotificationDetailResponse toDetail(PushNotification n) {
        NotificationDetailResponse r = new NotificationDetailResponse();
        r.setId(n.getId());
        r.setTitle(n.getTitle().value());
        r.setBody(n.getBody().value());
        r.setRecipientToken(n.getRecipientToken().value());
        r.setStatus(n.getStatus());
        r.setFcmMessageId(n.getFcmMessageId());
        r.setSentAt(n.getSentAt());
        r.setDeliveredAt(n.getDeliveredAt());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}
