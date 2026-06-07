package br.edu.acad.ifma.notification.adapter.persistence;

import br.edu.acad.ifma.notification.domain.FcmToken;
import br.edu.acad.ifma.notification.domain.NotificationBody;
import br.edu.acad.ifma.notification.domain.NotificationTitle;
import br.edu.acad.ifma.notification.domain.PushNotification;
import br.edu.acad.ifma.notification.port.NotificationRepositoryPort;
import br.edu.acad.ifma.notification.usecase.NotificationFilter;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository jpaRepository;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PushNotification save(PushNotification notification) {
        NotificationJpaEntity entity = toEntity(notification);
        NotificationJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PushNotification> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<PushNotification> findByFcmMessageId(String fcmMessageId) {
        return jpaRepository.findByFcmMessageId(fcmMessageId).map(this::toDomain);
    }

    @Override
    public Page<PushNotification> findAll(Pageable pageable, NotificationFilter filter) {
        return jpaRepository.findAllFiltered(filter.status(), filter.fcmToken(), pageable)
                .map(this::toDomain);
    }

    private NotificationJpaEntity toEntity(PushNotification n) {
        NotificationJpaEntity e = new NotificationJpaEntity();
        e.setId(n.getId());
        e.setTitle(n.getTitle().value());
        e.setBody(n.getBody().value());
        e.setRecipientToken(n.getRecipientToken().value());
        e.setStatus(n.getStatus());
        e.setFcmMessageId(n.getFcmMessageId());
        e.setSentAt(n.getSentAt());
        e.setDeliveredAt(n.getDeliveredAt());
        e.setCreatedAt(n.getCreatedAt());
        return e;
    }

    private PushNotification toDomain(NotificationJpaEntity e) {
        return PushNotification.builder()
                .withId(e.getId())
                .withTitle(new NotificationTitle(e.getTitle()))
                .withBody(new NotificationBody(e.getBody()))
                .withRecipientToken(new FcmToken(e.getRecipientToken()))
                .withStatus(e.getStatus())
                .withFcmMessageId(e.getFcmMessageId())
                .withSentAt(e.getSentAt())
                .withDeliveredAt(e.getDeliveredAt())
                .withCreatedAt(e.getCreatedAt())
                .build();
    }
}
