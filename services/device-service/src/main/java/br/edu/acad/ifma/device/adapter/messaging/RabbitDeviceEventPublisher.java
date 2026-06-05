package br.edu.acad.ifma.device.adapter.messaging;

import br.edu.acad.ifma.device.config.RabbitMQConfig;
import br.edu.acad.ifma.device.domain.Device;
import br.edu.acad.ifma.device.port.DeviceEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitDeviceEventPublisher implements DeviceEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitDeviceEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitDeviceEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishDeviceRegistered(Device device) {
        DeviceRegisteredEvent event = new DeviceRegisteredEvent(
                device.getId(),
                device.getFcmToken().value(),
                device.getType() != null ? device.getType().name() : null,
                device.getRegisteredAt()
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DEVICE_EXCHANGE,
                RabbitMQConfig.DEVICE_REGISTERED_ROUTING_KEY,
                event
        );
        log.info("Published DeviceRegistered event for device {}", device.getId());
    }
}
