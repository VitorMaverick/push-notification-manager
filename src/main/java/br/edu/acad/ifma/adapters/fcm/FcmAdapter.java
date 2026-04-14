package br.edu.acad.ifma.adapters.fcm;

import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.NotificationBody;
import br.edu.acad.ifma.app.domain.shared.NotificationTitle;
import br.edu.acad.ifma.app.domain.shared.exception.PushSendingException;
import br.edu.acad.ifma.app.port.PushSenderPort;
import br.edu.acad.ifma.service.notification.FcmService;
import br.edu.acad.ifma.service.notification.NotificationMessageTO;
import org.springframework.stereotype.Service;

@Service
public class FcmAdapter implements PushSenderPort {

    private final FcmService fcmService;

    public FcmAdapter(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @Override
    public String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body) {
        try {
            NotificationMessageTO msg = NotificationMessageTO.builder().titulo(title.value()).corpo(body.value()).build();
            return fcmService.sendToToken(token.value(), msg);
        } catch (Exception e) {
            throw new PushSendingException("FCM send failed: " + e.getMessage(), e);
        }
    }
}
