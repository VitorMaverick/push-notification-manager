package br.edu.acad.ifma.service.notification;

import br.edu.acad.ifma.domain.Notification;
import br.edu.acad.ifma.repository.NotificationMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("fcmNotificationStrategy")
public class FcmNotificationStrategy implements NotificationObserver {

    private final Logger log = LoggerFactory.getLogger(FcmNotificationStrategy.class);
    private final FcmService fcmService;
    private final NotificationMessageRepository repository;

    public FcmNotificationStrategy(FcmService fcmService, NotificationMessageRepository repository) {
        this.fcmService = fcmService;
        this.repository = repository;
    }

    @Override
    @Transactional
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
            // persist the FCM message id on the notification record
            message.setFcmMessageId(resp);
            repository.save(message);
        } catch (Exception e) {
            log.warn("Failed to send FCM message for id={} token={}: {}", message.getId(), token, e.getMessage());
        }
    }
}
