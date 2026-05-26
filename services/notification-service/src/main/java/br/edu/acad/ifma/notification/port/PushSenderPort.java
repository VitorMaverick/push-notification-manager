package br.edu.acad.ifma.notification.port;

import br.edu.acad.ifma.notification.domain.FcmToken;
import br.edu.acad.ifma.notification.domain.NotificationBody;
import br.edu.acad.ifma.notification.domain.NotificationTitle;
import java.util.Map;

public interface PushSenderPort {
    String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body);
    String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body, Map<String, String> data);
}
