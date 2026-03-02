package br.edu.acad.ifma.service.notification;

import br.edu.acad.ifma.domain.NotificationChannel;
import br.edu.acad.ifma.domain.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component("emailNotificationStrategy")
public class EmailNotificationStrategy implements NotificationObserver {

    private final Logger log = LoggerFactory.getLogger(EmailNotificationStrategy.class);
    private final JavaMailSender mailSender;

    public EmailNotificationStrategy(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void onNotify(NotificationMessage message) {
        // For demonstration, we'll log and send a very simple mail if mailSender is configured
        if (message == null) return;
        log.info("Sending email notification: {}", message.getSubject());
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo("placeholder@example.com"); // TODO: replace with real recipient
            mail.setSubject(message.getSubject());
            mail.setText(message.getBody());
            mailSender.send(mail);
            log.info("Email sent (simulated) for message id={}", message.getId());
        } catch (Exception e) {
            log.warn("Failed to send email notification: {}", e.getMessage());
        }
    }
}
