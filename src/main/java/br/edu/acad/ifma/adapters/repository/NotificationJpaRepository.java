package br.edu.acad.ifma.adapters.repository;

import br.edu.acad.ifma.adapters.model.NotificationJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationJpaRepository
    extends JpaRepository<NotificationJpaEntity, Long>, JpaSpecificationExecutor<NotificationJpaEntity> {
    Optional<NotificationJpaEntity> findByFcmMessageId(String fcmMessageId);
}
