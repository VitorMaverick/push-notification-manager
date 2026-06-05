package br.edu.acad.ifma.notification.usecase;

import br.edu.acad.ifma.notification.domain.PushNotification;
import br.edu.acad.ifma.notification.port.NotificationRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetNotificationHistoryUseCase {

    private final NotificationRepositoryPort repository;

    public GetNotificationHistoryUseCase(NotificationRepositoryPort repository) {
        this.repository = repository;
    }

    public Page<PushNotification> execute(Pageable pageable, NotificationFilter filter) {
        return repository.findAll(pageable, filter);
    }
}
