package br.edu.acad.ifma.service.notification;

import br.edu.acad.ifma.domain.Notification;

public interface NotificationObserver {
    void onNotify(Notification message);
}
