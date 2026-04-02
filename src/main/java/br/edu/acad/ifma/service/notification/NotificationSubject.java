package br.edu.acad.ifma.service.notification;

import br.edu.acad.ifma.domain.Notification;

public interface NotificationSubject {
    void registerObserver(NotificationObserver observer);
    void removeObserver(NotificationObserver observer);
    void notifyObservers(Notification message);
}
