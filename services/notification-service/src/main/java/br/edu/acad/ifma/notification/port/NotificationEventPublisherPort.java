package br.edu.acad.ifma.notification.port;

import br.edu.acad.ifma.notification.adapter.messaging.SendNotificationFailedEvent;

public interface NotificationEventPublisherPort {
    void publishSendNotificationFailed(SendNotificationFailedEvent event);
}
