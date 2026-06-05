package br.edu.acad.ifma.notification.adapter.messaging;

import br.edu.acad.ifma.notification.config.RabbitMQConfig;
import br.edu.acad.ifma.notification.domain.FcmToken;
import br.edu.acad.ifma.notification.domain.SendNotificationFailedEvent;
import br.edu.acad.ifma.notification.domain.NotificationBody;
import br.edu.acad.ifma.notification.domain.NotificationTitle;
import br.edu.acad.ifma.notification.domain.PushSendingException;
import br.edu.acad.ifma.notification.port.NotificationRepositoryPort;
import br.edu.acad.ifma.notification.port.PushSenderPort;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationRetryConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationRetryConsumer.class);

    private final PushSenderPort pushSender;
    private final NotificationRepositoryPort repository;
    private final RabbitTemplate rabbitTemplate;

    public NotificationRetryConsumer(
            PushSenderPort pushSender,
            NotificationRepositoryPort repository,
            RabbitTemplate rabbitTemplate) {
        this.pushSender = pushSender;
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_RETRY_PROCESS_QUEUE)
    public void onRetry(SendNotificationFailedEvent event) {
        log.info("Retrying notification {} attempt {}/{}",
                event.getNotificationId(), event.getAttemptCount(), RabbitMQConfig.MAX_RETRY_ATTEMPTS);

        if (event.getAttemptCount() > RabbitMQConfig.MAX_RETRY_ATTEMPTS) {
            moveToDlq(event, "Max retry attempts exceeded");
            return;
        }

        try {
            FcmToken token = new FcmToken(event.getRecipientToken());
            NotificationTitle title = new NotificationTitle(event.getTitle());
            NotificationBody body = new NotificationBody(event.getBody());

            String messageId = event.getData() != null && !event.getData().isEmpty()
                    ? pushSender.sendPushNotification(token, title, body, event.getData())
                    : pushSender.sendPushNotification(token, title, body);

            repository.findById(event.getNotificationId()).ifPresent(notification -> {
                notification.markSent(messageId);
                repository.save(notification);
            });

            log.info("Retry successful for notification {} — FCM id {}", event.getNotificationId(), messageId);

        } catch (PushSendingException e) {
            log.warn("Retry {} failed for notification {}: {}", event.getAttemptCount(), event.getNotificationId(), e.getMessage());

            if (isPermanentFailure(e.getMessage())) {
                moveToDlq(event, "Permanent failure: " + e.getMessage());
                deactivateDeviceIfPossible(event.getRecipientToken());
            } else {
                scheduleNextRetry(event, e.getMessage());
            }
        }
    }

    private void scheduleNextRetry(SendNotificationFailedEvent event, String reason) {
        SendNotificationFailedEvent nextAttempt = event.withIncrementedAttempt(reason);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_RETRY_EXCHANGE,
                RabbitMQConfig.ROUTING_RETRY,
                nextAttempt
        );
        log.info("Scheduled retry {} for notification {}", nextAttempt.getAttemptCount(), event.getNotificationId());
    }

    private void moveToDlq(SendNotificationFailedEvent event, String reason) {
        log.error("Moving notification {} to DLQ after {} attempts. Reason: {}",
                event.getNotificationId(), event.getAttemptCount(), reason);
        repository.findById(event.getNotificationId()).ifPresent(notification -> {
            notification.markFailed("DLQ: " + reason);
            repository.save(notification);
        });
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_RETRY_EXCHANGE,
                RabbitMQConfig.ROUTING_RETRY_DLQ,
                event
        );
    }

    private boolean isPermanentFailure(String reason) {
        if (reason == null) return false;
        String lower = reason.toLowerCase();
        return lower.contains("invalid-argument")
                || lower.contains("registration-token-not-registered")
                || lower.contains("unregistered");
    }

    private void deactivateDeviceIfPossible(String fcmToken) {
        // Note: cross-service deactivation would require publishing a DeviceTokenInvalidated
        // event to device.exchange, consumed by device-service to mark device as INACTIVE.
        // For current scope, we log the recommendation.
        log.warn("FCM token {} is permanently invalid. Consider publishing DeviceTokenInvalidated event.", fcmToken);
    }
}
