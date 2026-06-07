package br.edu.acad.ifma.notification.adapter.messaging;

import br.edu.acad.ifma.notification.config.RabbitMQConfig;
import br.edu.acad.ifma.notification.usecase.SendPushNotificationCommand;
import br.edu.acad.ifma.notification.usecase.SendPushNotificationUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeviceRegisteredConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeviceRegisteredConsumer.class);

    private final SendPushNotificationUseCase sendUseCase;

    public DeviceRegisteredConsumer(SendPushNotificationUseCase sendUseCase) {
        this.sendUseCase = sendUseCase;
    }

    @RabbitListener(queues = RabbitMQConfig.DEVICE_REGISTERED_QUEUE)
    public void onDeviceRegistered(DeviceRegisteredEvent event) {
        log.info("Received DeviceRegistered event for device {} token {}",
                event.getDeviceId(), event.getFcmToken());

        SendPushNotificationCommand welcomeCommand = new SendPushNotificationCommand(
                event.getFcmToken(),
                "Welcome!",
                "Your device has been successfully registered for push notifications.",
                null
        );

        try {
            sendUseCase.execute(welcomeCommand);
            log.info("Welcome notification dispatched for device {}", event.getDeviceId());
        } catch (Exception e) {
            log.error("Failed to send welcome notification for device {}: {}", event.getDeviceId(), e.getMessage());
        }
    }
}
