package br.edu.acad.ifma.service.notification;

import br.edu.acad.ifma.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("fcmNotificationStrategy")
public class FcmNotificationStrategy implements NotificationObserver {

    private final Logger log = LoggerFactory.getLogger(FcmNotificationStrategy.class);
    private final FcmService fcmService;

    public FcmNotificationStrategy(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @Override
    public void onNotify(Notification message) {
        if (message == null) return;
        // Use recipientToken from NotificationMessage to send via FcmService
        String token = message.getRecipientToken();
        if (token == null || token.isBlank()) {
            log.warn("No recipient token for FCM message id={}", message.getId());
            return;
        }
        NotificationMessageTO notificationMessageTO = NotificationMessageTO.builder()
            .titulo(message.getSubject())
            .corpo(message.getBody())
            .build();
        try {
            String resp = fcmService.sendToToken(token, notificationMessageTO);
            log.info("Triggered FCM send for message id={} token={} response={}", message.getId(), token, resp);
        } catch (Exception e) {
            log.warn("Failed to send FCM message for id={} token={}: {}", message.getId(), token, e.getMessage());
        }
    }
}
