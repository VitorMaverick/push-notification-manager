package br.edu.acad.ifma.notification.adapter.fcm;

import br.edu.acad.ifma.notification.domain.FcmToken;
import br.edu.acad.ifma.notification.domain.NotificationBody;
import br.edu.acad.ifma.notification.domain.NotificationTitle;
import br.edu.acad.ifma.notification.domain.PushSendingException;
import br.edu.acad.ifma.notification.port.PushSenderPort;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FcmService implements PushSenderPort {

    private static final Logger log = LoggerFactory.getLogger(FcmService.class);

    private final FirebaseMessaging firebaseMessaging;

    public FcmService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    @Override
    public String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body) {
        return send(buildMessage(token, title, body, null));
    }

    @Override
    public String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body, Map<String, String> data) {
        return send(buildMessage(token, title, body, data));
    }

    private Message buildMessage(FcmToken token, NotificationTitle title, NotificationBody body, Map<String, String> data) {
        Message.Builder builder = Message.builder()
                .setToken(token.value())
                .setNotification(Notification.builder()
                        .setTitle(title.value())
                        .setBody(body.value())
                        .build());

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        return builder.build();
    }

    private String send(Message message) {
        try {
            String messageId = firebaseMessaging.send(message);
            log.debug("FCM message sent: {}", messageId);
            return messageId;
        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed: {}", e.getMessage());
            throw new PushSendingException("FCM delivery failed: " + e.getMessage(), e);
        }
    }
}
