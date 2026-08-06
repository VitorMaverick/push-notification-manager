package br.edu.acad.ifma.notification.usecase;

import br.edu.acad.ifma.notification.domain.PushNotification;
import br.edu.acad.ifma.notification.domain.NotificationStatus;
import br.edu.acad.ifma.notification.port.NotificationRepositoryPort;
import br.edu.acad.ifma.notification.port.PushSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AsyncNotificationService.class);

    private final PushSenderPort pushSenderPort;
    private final NotificationRepositoryPort repositoryPort;

    public AsyncNotificationService(PushSenderPort pushSenderPort,
                                    NotificationRepositoryPort repositoryPort) {
        this.pushSenderPort = pushSenderPort;
        this.repositoryPort = repositoryPort;
    }

    @Async("notificationExecutor")
    public void sendInBackground(PushNotification notification) {
        try {
            String messageId = pushSenderPort.sendPushNotification(
                notification.getRecipientToken(),
                notification.getTitle(),
                notification.getBody()
            );
            notification.markSent(messageId);
            log.info("Notification {} sent successfully, messageId={}", notification.getId(), messageId);
        } catch (Exception e) {
            notification.markFailed(e.getMessage());
            log.error("Failed to send notification {}: {}", notification.getId(), e.getMessage());
        } finally {
            repositoryPort.save(notification);
        }
    }
}
