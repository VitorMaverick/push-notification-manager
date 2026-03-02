package br.edu.acad.ifma.service.notification;

import br.edu.acad.ifma.domain.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("smsNotificationStrategy")
public class SmsNotificationStrategy implements NotificationObserver {

    private final Logger log = LoggerFactory.getLogger(SmsNotificationStrategy.class);

    @Override
    public void onNotify(NotificationMessage message) {
        if (message == null) return;
        // Integrate with an SMS provider here. For now, we just log.
        log.info("Sending SMS notification: {} - {}", message.getSubject(), message.getBody());
    }
}
