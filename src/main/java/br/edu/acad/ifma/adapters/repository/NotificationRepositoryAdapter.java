package br.edu.acad.ifma.adapters.repository;

import br.edu.acad.ifma.adapters.factory.NotificationFactory;
import br.edu.acad.ifma.adapters.model.NotificationJpaEntity;
import br.edu.acad.ifma.app.domain.notification.PushNotification;
import br.edu.acad.ifma.app.port.NotificationRepositoryPort;
import br.edu.acad.ifma.app.usecase.notification.NotificationFilter;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository jpaRepository;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PushNotification save(PushNotification notification) {
        NotificationJpaEntity entity = NotificationFactory.toEntity(notification);
        return NotificationFactory.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<PushNotification> findById(Long id) {
        return jpaRepository.findById(id).map(NotificationFactory::toDomain);
    }

    @Override
    public Optional<PushNotification> findByFcmMessageId(String fcmMessageId) {
        return jpaRepository.findByFcmMessageId(fcmMessageId).map(NotificationFactory::toDomain);
    }

    @Override
    public Page<PushNotification> findAll(Pageable pageable, NotificationFilter filter) {
        Specification<NotificationJpaEntity> spec = toSpec(filter);
        return jpaRepository.findAll(spec, pageable).map(NotificationFactory::toDomain);
    }

    private Specification<NotificationJpaEntity> toSpec(NotificationFilter filter) {
        Specification<NotificationJpaEntity> spec = Specification.where(null);
        if (filter.getStatus() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getDeviceToken() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("recipientToken"), filter.getDeviceToken()));
        }
        if (filter.getFromDate() != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
        }
        if (filter.getToDate() != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
        }
        return spec;
    }
}
