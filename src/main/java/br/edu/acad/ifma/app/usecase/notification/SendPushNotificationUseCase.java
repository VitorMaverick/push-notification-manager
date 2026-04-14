package br.edu.acad.ifma.app.usecase.notification;

import br.edu.acad.ifma.app.domain.notification.NotificationStatus;
import br.edu.acad.ifma.app.domain.notification.PushNotification;
import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.NotificationBody;
import br.edu.acad.ifma.app.domain.shared.NotificationTitle;
import br.edu.acad.ifma.app.domain.shared.exception.PushSendingException;
import br.edu.acad.ifma.app.port.NotificationRepositoryPort;
import br.edu.acad.ifma.app.port.PushSenderPort;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SendPushNotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final PushSenderPort pushSender;

    public SendPushNotificationUseCase(NotificationRepositoryPort notificationRepository, PushSenderPort pushSender) {
        this.notificationRepository = notificationRepository;
        this.pushSender = pushSender;
    }

    public PushNotification execute(SendPushNotificationCommand command) {
        FcmToken token = new FcmToken(command.getDeviceToken());
        NotificationTitle title = new NotificationTitle(command.getTitle());
        NotificationBody body = new NotificationBody(command.getBody());

        PushNotification notification = PushNotification.builder()
            .withRecipientToken(token)
            .withTitle(title)
            .withBody(body)
            .withStatus(NotificationStatus.PENDING)
            .withCreatedAt(Instant.now())
            .build();
        notification = notificationRepository.save(notification);

        try {
            String fcmId = pushSender.sendPushNotification(token, title, body);
            notification.markSent(fcmId);
        } catch (PushSendingException e) {
            notification.markFailed(e.getMessage());
            notificationRepository.save(notification);
            throw e;
        }
        return notificationRepository.save(notification);
    }
}
