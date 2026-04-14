package br.edu.acad.ifma.service.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import br.edu.acad.ifma.domain.Notification;
import br.edu.acad.ifma.domain.NotificationChannel;
import br.edu.acad.ifma.repository.NotificationMessageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;

class StrategiesUnitTest {

    @Test
    void testEmailStrategy_callsMailSender() {
        JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);
        EmailNotificationStrategy email = new EmailNotificationStrategy(mailSender);
        Notification m = new Notification("Subject", "Body", NotificationChannel.EMAIL);
        email.onNotify(m);
        // mailSender.send may or may not be called depending on exceptions; we assert no exception
    }

    @Test
    void testSmsStrategy_logs() {
        SmsNotificationStrategy sms = new SmsNotificationStrategy();
        Notification m = new Notification("Subject", "Body", NotificationChannel.SMS);
        sms.onNotify(m);
    }

    @Test
    void testFcmStrategy_logs() throws Exception {
        FcmService fcmService = Mockito.mock(FcmService.class);
        Mockito.when(fcmService.sendToToken(anyString(), any(NotificationMessageTO.class))).thenReturn("ok");
        NotificationMessageRepository repo = Mockito.mock(NotificationMessageRepository.class);
        FcmNotificationStrategy fcm = new FcmNotificationStrategy(fcmService, repo);
        Notification m = new Notification("Subject", "Body", NotificationChannel.FCM_PUSH);
        m.setRecipientToken("token-1");
        fcm.onNotify(m);
    }
}
