package br.edu.acad.ifma.notification.usecase;

import br.edu.acad.ifma.notification.domain.PushNotification;
import br.edu.acad.ifma.notification.port.NotificationRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarkNotificationDeliveredUseCase {

    private static final Logger log = LoggerFactory.getLogger(MarkNotificationDeliveredUseCase.class);

    private final NotificationRepositoryPort repository;

    public MarkNotificationDeliveredUseCase(NotificationRepositoryPort repository) {
        this.repository = repository;
    }

    public PushNotification execute(Long notificationId) {
        PushNotification notification = repository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));

        notification.markDelivered();
        PushNotification updated = repository.save(notification);
        log.info("Notification {} marked as delivered", updated.getId());
        return updated;
    }
}
