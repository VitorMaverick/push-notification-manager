package br.edu.acad.ifma.app.usecase.notification;

import br.edu.acad.ifma.app.domain.notification.PushNotification;
import br.edu.acad.ifma.app.domain.shared.exception.NotificationNotFoundException;
import br.edu.acad.ifma.app.port.NotificationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetNotificationByIdUseCase {

    private final NotificationRepositoryPort notificationRepository;

    public GetNotificationByIdUseCase(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public PushNotification execute(Long id) {
        return notificationRepository.findById(id).orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + id));
    }
}
