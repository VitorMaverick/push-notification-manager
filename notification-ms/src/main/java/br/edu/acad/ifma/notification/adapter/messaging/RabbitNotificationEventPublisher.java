package br.edu.acad.ifma.notification.adapter.messaging;

import br.edu.acad.ifma.notification.config.RabbitMQConfig;
import br.edu.acad.ifma.notification.domain.SendNotificationFailedEvent;
import br.edu.acad.ifma.notification.port.NotificationEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitNotificationEventPublisher implements NotificationEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitNotificationEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitNotificationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishSendNotificationFailed(SendNotificationFailedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_RETRY_EXCHANGE,
                RabbitMQConfig.ROUTING_RETRY,
                event
        );
        log.warn("Published SendNotificationFailed for notification {} attempt {}",
                event.getNotificationId(), event.getAttemptCount());
    }
}
