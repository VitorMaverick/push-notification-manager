package br.edu.acad.ifma.service.notification;

import br.edu.acad.ifma.domain.NotificationMessage;

public interface NotificationSubject {
    void registerObserver(NotificationObserver observer);
    void removeObserver(NotificationObserver observer);
    void notifyObservers(NotificationMessage message);
}
