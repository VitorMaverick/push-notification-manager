package br.edu.acad.ifma.app.port;

import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.NotificationBody;
import br.edu.acad.ifma.app.domain.shared.NotificationTitle;

public interface PushSenderPort {
    String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body);
}
