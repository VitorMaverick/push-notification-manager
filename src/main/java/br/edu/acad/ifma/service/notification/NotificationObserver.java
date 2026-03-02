package br.edu.acad.ifma.service.notification;

import br.edu.acad.ifma.domain.NotificationMessage;

public interface NotificationObserver {
    void onNotify(NotificationMessage message);
}
