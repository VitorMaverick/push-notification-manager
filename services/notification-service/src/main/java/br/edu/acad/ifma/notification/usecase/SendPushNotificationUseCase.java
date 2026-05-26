package br.edu.acad.ifma.notification.usecase;

import br.edu.acad.ifma.notification.domain.FcmToken;
import br.edu.acad.ifma.notification.domain.NotificationBody;
import br.edu.acad.ifma.notification.domain.NotificationStatus;
import br.edu.acad.ifma.notification.domain.NotificationTitle;
import br.edu.acad.ifma.notification.domain.PushNotification;
import br.edu.acad.ifma.notification.domain.PushSendingException;
import br.edu.acad.ifma.notification.port.NotificationRepositoryPort;
import br.edu.acad.ifma.notification.port.PushSenderPort;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SendPushNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendPushNotificationUseCase.class);

    private final NotificationRepositoryPort repository;
    private final PushSenderPort pushSender;

    public SendPushNotificationUseCase(NotificationRepositoryPort repository, PushSenderPort pushSender) {
        this.repository = repository;
        this.pushSender = pushSender;
    }

    public PushNotification execute(SendPushNotificationCommand command) {
        FcmToken token = new FcmToken(command.recipientToken());
        NotificationTitle title = new NotificationTitle(command.title());
        NotificationBody body = new NotificationBody(command.body());

        PushNotification notification = PushNotification.builder()
                .withRecipientToken(token)
                .withTitle(title)
                .withBody(body)
                .withStatus(NotificationStatus.PENDING)
                .withCreatedAt(Instant.now())
                .build();

        PushNotification saved = repository.save(notification);

        try {
            String messageId = command.data() != null && !command.data().isEmpty()
                    ? pushSender.sendPushNotification(token, title, body, command.data())
                    : pushSender.sendPushNotification(token, title, body);

            saved.markSent(messageId);
            log.info("Notification {} sent with FCM id {}", saved.getId(), messageId);
        } catch (PushSendingException e) {
            saved.markFailed(e.getMessage());
            log.error("Notification {} failed: {}", saved.getId(), e.getMessage());
        }

        return repository.save(saved);
    }
}
