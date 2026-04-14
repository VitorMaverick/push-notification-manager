package br.edu.acad.ifma.app.port;

import br.edu.acad.ifma.app.domain.notification.PushNotification;
import br.edu.acad.ifma.app.usecase.notification.NotificationFilter;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryPort {
    PushNotification save(PushNotification notification);

    Optional<PushNotification> findById(Long id);

    Optional<PushNotification> findByFcmMessageId(String fcmMessageId);

    Page<PushNotification> findAll(Pageable pageable, NotificationFilter filter);
}
