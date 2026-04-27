package br.edu.acad.ifma.app.port;

import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.NotificationBody;
import br.edu.acad.ifma.app.domain.shared.NotificationTitle;
import java.util.Map;

public interface PushSenderPort {
    String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body);
    String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body, Map<String, String> data);
}
