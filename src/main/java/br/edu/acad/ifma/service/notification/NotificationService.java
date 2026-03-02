package br.edu.acad.ifma.service.notification;

import br.edu.acad.ifma.domain.NotificationChannel;
import br.edu.acad.ifma.domain.NotificationMessage;
import br.edu.acad.ifma.repository.NotificationMessageRepository;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService implements NotificationSubject {

    private final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationMessageRepository repository;
    private final Set<NotificationObserver> observers = Collections.synchronizedSet(new HashSet<>());

    @Autowired
    public NotificationService(NotificationMessageRepository repository, List<NotificationObserver> observerList) {
        this.repository = repository;
        List<NotificationObserver> observersToRegister = observerList == null ? Collections.emptyList() : observerList;
        for (NotificationObserver observer : observersToRegister) {
            registerObserver(observer);
        }
        if (!observersToRegister.isEmpty()) {
            log.info("Registered {} NotificationObserver bean(s)", observersToRegister.size());
        }
    }

    @Override
    public void registerObserver(NotificationObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(NotificationMessage message) {
        // copy to avoid ConcurrentModificationException
        List<NotificationObserver> copy;
        synchronized (observers) {
            copy = new ArrayList<>(observers);
        }
        for (NotificationObserver o : copy) {
            try {
                o.onNotify(message);
            } catch (Exception e) {
                log.warn("Observer threw exception: {}", e.getMessage());
            }
        }
    }

    @Transactional
    public NotificationMessage createAndSend(NotificationMessage message) {
        // persist
        NotificationMessage saved = repository.save(message);
        log.info("Saved notification message id={}", saved.getId());
        // notify observers (they will handle sending to their respective channels)
        notifyObservers(saved);
        return saved;
    }
}
