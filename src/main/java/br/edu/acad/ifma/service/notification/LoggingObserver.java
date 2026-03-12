package br.edu.acad.ifma.service.notification;

import br.edu.acad.ifma.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingObserver implements NotificationObserver {

    private final Logger log = LoggerFactory.getLogger(LoggingObserver.class);

    @Override
    public void onNotify(Notification message) {
        log.info("Observer - new notification: {} - {}", message.getSubject(), message.getChannel());
    }
}
