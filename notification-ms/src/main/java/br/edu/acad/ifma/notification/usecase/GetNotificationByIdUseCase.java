package br.edu.acad.ifma.notification.usecase;

import br.edu.acad.ifma.notification.domain.PushNotification;
import br.edu.acad.ifma.notification.port.NotificationRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetNotificationByIdUseCase {

    private final NotificationRepositoryPort repository;

    public GetNotificationByIdUseCase(NotificationRepositoryPort repository) {
        this.repository = repository;
    }

    public PushNotification execute(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + id));
    }
}
