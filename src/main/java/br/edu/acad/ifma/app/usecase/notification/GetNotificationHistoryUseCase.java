package br.edu.acad.ifma.app.usecase.notification;

import br.edu.acad.ifma.app.domain.notification.PushNotification;
import br.edu.acad.ifma.app.domain.shared.exception.DomainException;
import br.edu.acad.ifma.app.port.NotificationRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetNotificationHistoryUseCase {

    private final NotificationRepositoryPort notificationRepository;

    public GetNotificationHistoryUseCase(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Page<PushNotification> execute(NotificationHistoryQuery query) {
        NotificationFilter filter = query.getFilter();
        if (query.getPageable().getPageSize() > 500) {
            throw new DomainException("Page size must not exceed 500");
        }
        if (filter.getFromDate() != null && filter.getToDate() != null && filter.getFromDate().isAfter(filter.getToDate())) {
            throw new DomainException("fromDate must not be after toDate");
        }
        return notificationRepository.findAll(query.getPageable(), filter);
    }
}
