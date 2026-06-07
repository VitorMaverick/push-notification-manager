package br.edu.acad.ifma.notification.port;

import br.edu.acad.ifma.notification.domain.SendNotificationFailedEvent;

public interface NotificationEventPublisherPort {
    void publishSendNotificationFailed(SendNotificationFailedEvent event);
}
